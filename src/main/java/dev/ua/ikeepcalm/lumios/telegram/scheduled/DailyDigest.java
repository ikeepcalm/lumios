package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.PersonalTimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * One private message each morning listing the member's own classes for the day.
 * <p>
 * The per-class reminders are the noisy half of the feature: a group with three elective slots
 * interrupts five times before lunch. A member who would rather glance at the day once gets this
 * instead, or as well.
 * <p>
 * Every hour the settings menu offers falls on a half hour, so firing at :00 and :30 between 06:00 and
 * 09:00 covers all of them in eight runs a day. As with {@link ClassNotification}, the cron must
 * declare {@link TimetableClock#ZONE_ID} or it is evaluated in the container's UTC.
 */
@Slf4j
@Component
public class DailyDigest {

    private final TelegramClient telegramClient;
    private final TranslationService translationService;
    private final PersonalTimetableSupport personalTimetableSupport;
    private final PersonalTimetableService personalTimetableService;

    /**
     * Guards against a second send when the application restarts inside the same half hour. Keyed by
     * member and chat, holding the day already covered.
     */
    private final Cache<String, LocalDate> sent = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.DAYS)
            .maximumSize(10_000)
            .build();

    public DailyDigest(TelegramClient telegramClient, TranslationService translationService,
                       PersonalTimetableSupport personalTimetableSupport,
                       PersonalTimetableService personalTimetableService) {
        this.telegramClient = telegramClient;
        this.translationService = translationService;
        this.personalTimetableSupport = personalTimetableSupport;
        this.personalTimetableService = personalTimetableService;
    }

    @Scheduled(cron = "0 0,30 6-9 * * MON-SAT", zone = TimetableClock.ZONE_ID)
    public void sendDigests() {
        LocalTime minute = TimetableClock.currentMinute();
        LocalDate date = TimetableClock.today();

        List<TimetableMember> due;
        try {
            due = personalTimetableService.digestMembersAt(minute);
        } catch (Exception e) {
            log.error("Could not load the members due a digest at {}", minute, e);
            return;
        }
        if (due.isEmpty()) {
            return;
        }

        int delivered = 0;
        for (Map.Entry<Long, List<TimetableMember>> group : groupByChat(due).entrySet()) {
            delivered += sendForChat(group.getKey(), group.getValue(), date);
        }
        if (delivered > 0) {
            log.info("Sent {} morning digests at {}", delivered, minute);
        }
    }

    /**
     * The chat's classes for today are loaded once, and so are its members' choices, however many
     * digests come out of it.
     */
    private int sendForChat(Long chatId, List<TimetableMember> members, LocalDate date) {
        LumiosChat chat = personalTimetableSupport.chatOrNull(chatId);
        if (chat == null || !chat.isTimetableEnabled()) {
            return 0;
        }

        List<ClassEntry> today = classesToday(chatId, date);
        if (today.isEmpty()) {
            return 0;
        }

        Map<Long, Set<String>> choices;
        try {
            choices = personalTimetableService.chosenSubjectsByMember(chatId);
        } catch (Exception e) {
            log.error("Could not load the elective choices of chat {}", chatId, e);
            return 0;
        }

        int delivered = 0;
        for (TimetableMember member : members) {
            String cacheKey = member.getTelegramUserId() + "_" + chatId;
            if (date.equals(sent.getIfPresent(cacheKey))) {
                continue;
            }

            List<ClassEntry> mine = personalTimetableSupport.personalDay(chatId, today,
                    choices.getOrDefault(member.getTelegramUserId(), Set.of()));
            // A member whose whole day is other people's electives hears nothing rather than a message
            // saying so - a digest is a convenience, not a report.
            if (mine.isEmpty()) {
                sent.put(cacheKey, date);
                continue;
            }

            sent.put(cacheKey, date);
            TextMessage message = ClassMarkupUtil.createDigest(mine,
                    personalTimetableSupport.languageChatFor(member.getTelegramUserId(), chat),
                    member.getTelegramUserId(), translationService);
            if (telegramClient.trySendTextMessage(message).blocked()) {
                log.info("Member {} has blocked the bot; muting their digest", member.getTelegramUserId());
                personalTimetableService.markDmUnavailable(chatId, member.getTelegramUserId());
                continue;
            }
            delivered++;
        }
        return delivered;
    }

    private List<ClassEntry> classesToday(Long chatId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<ClassEntry> classes = new ArrayList<>();
        for (TimetableEntry timetable : personalTimetableSupport.timetablesOf(chatId)) {
            if (!WeekValidator.determineWeekType(date).equals(timetable.getWeekType())) {
                continue;
            }
            for (DayEntry day : timetable.getDays()) {
                if (dayOfWeek.equals(day.getDayName()) && day.getClassEntries() != null) {
                    classes.addAll(day.getClassEntries());
                }
            }
        }
        return classes;
    }

    private Map<Long, List<TimetableMember>> groupByChat(List<TimetableMember> members) {
        Map<Long, List<TimetableMember>> byChat = new LinkedHashMap<>();
        for (TimetableMember member : members) {
            byChat.computeIfAbsent(member.getChatId(), id -> new ArrayList<>()).add(member);
        }
        return byChat;
    }
}
