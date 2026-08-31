package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.ClassEntryRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.PersonalTimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends class reminders to group chats: one when a slot starts, and one a configurable number of
 * minutes beforehand.
 * <p>
 * All timing runs on {@link TimetableClock}. The cron below must declare the same zone, or the window
 * is evaluated in the container's UTC while the class times are Kyiv local - which is what previously
 * kept the 08:30 slot, the most common first class of the day, from ever being announced.
 * <p>
 * Reminders are aggregated per time slot rather than sent per class. A group timetable imported from
 * campus contains every parallel elective in a slot - up to six for some groups - and announcing them
 * individually buries the chat.
 */
@Slf4j
@Component
public class ClassNotification {

    /**
     * Longest advance warning a chat may configure; also the look-ahead of the query below.
     */
    public static final int MAX_LEAD_MINUTES = 60;

    private final TelegramClient telegramClient;
    private final ClassEntryRepository classEntryRepository;
    private final TranslationService translationService;
    private final PersonalTimetableSupport personalTimetableSupport;
    private final PersonalTimetableService personalTimetableService;

    /**
     * Guards against announcing the same slot twice in one day. Class start times are whole minutes
     * and the query matches an exact minute, so this only has to cover a restart or an overlapping
     * run inside the same minute.
     */
    private final Map<String, LocalDate> notificationCache = new ConcurrentHashMap<>();

    public ClassNotification(TelegramClient telegramClient, ClassEntryRepository classEntryRepository,
                             TranslationService translationService, PersonalTimetableSupport personalTimetableSupport,
                             PersonalTimetableService personalTimetableService) {
        this.telegramClient = telegramClient;
        this.classEntryRepository = classEntryRepository;
        this.translationService = translationService;
        this.personalTimetableSupport = personalTimetableSupport;
        this.personalTimetableService = personalTimetableService;
    }

    /**
     * Runs every minute across the whole teaching day, Monday to Saturday - campus does schedule
     * Saturday classes, which the previous MON-FRI window silently dropped.
     */
    // Deliberately not @Transactional: every association walked below (class -> day -> timetable ->
    // chat) is @ManyToOne and therefore eager, so a transaction would only pin a pooled connection
    // for the duration of the outgoing Telegram calls.
    @Scheduled(cron = "0 * 6-22 * * MON-SAT", zone = TimetableClock.ZONE_ID)
    public void checkForUpcomingClasses() {
        LocalTime minute = TimetableClock.currentMinute();
        LocalDate date = TimetableClock.today();
        DayOfWeek today = date.getDayOfWeek();
        WeekType weekType = WeekValidator.determineWeekType(date);

        try {
            List<ClassEntry> upcoming = classEntryRepository.findClassesStartingBetween(
                    minute, minute.plusMinutes(MAX_LEAD_MINUTES), today);

            int announced = 0;
            for (Map.Entry<SlotKey, List<ClassEntry>> slot : groupIntoSlots(upcoming, weekType).entrySet()) {
                announced += announce(slot.getKey(), slot.getValue(), minute, date) ? 1 : 0;
                remindMembers(slot.getKey(), slot.getValue(), minute, date);
            }

            if (announced > 0) {
                log.info("Announced {} class slot(s) at {} {}", announced, date, minute);
            }
        } catch (Exception e) {
            log.error("Error during class notification check", e);
        }
    }

    /**
     * Buckets the due classes by chat and start time, dropping other-week entries and duplicates.
     */
    private Map<SlotKey, List<ClassEntry>> groupIntoSlots(List<ClassEntry> classes, WeekType weekType) {
        Map<SlotKey, List<ClassEntry>> slots = new LinkedHashMap<>();
        for (ClassEntry classEntry : classes) {
            if (classEntry.getDayEntry() == null || classEntry.getDayEntry().getTimetableEntry() == null) {
                continue;
            }
            if (!weekType.equals(classEntry.getDayEntry().getTimetableEntry().getWeekType())) {
                continue;
            }
            LumiosChat chat = classEntry.getDayEntry().getTimetableEntry().getChat();
            if (chat == null || !chat.isTimetableEnabled()) {
                continue;
            }
            slots.computeIfAbsent(new SlotKey(chat.getChatId(), classEntry.getStartTime()), key -> new ArrayList<>())
                    .add(classEntry);
        }
        slots.values().forEach(entries -> {
            entries.sort(Comparator.comparing(entry -> entry.getName() == null ? "" : entry.getName()));
            deduplicate(entries);
        });
        return slots;
    }

    /**
     * The campus feed sometimes lists the very same class twice in one slot (same subject, same
     * teacher); announcing it twice looks like a bug to the reader.
     */
    private void deduplicate(List<ClassEntry> entries) {
        Set<String> seen = new LinkedHashSet<>();
        entries.removeIf(entry -> !seen.add(
                entry.getName() + "|" + entry.getClassType() + "|" + entry.getTeacherName()));
    }

    /**
     * @return true when a message was sent for this slot
     */
    private boolean announce(SlotKey slot, List<ClassEntry> entries, LocalTime minute, LocalDate date) {
        LumiosChat chat = entries.getFirst().getDayEntry().getTimetableEntry().getChat();
        long minutesAway = ChronoUnit.MINUTES.between(minute, slot.startTime());

        Reminder reminder;
        if (minutesAway == 0) {
            reminder = Reminder.STARTING;
        } else if (minutesAway == leadMinutes(chat)) {
            reminder = Reminder.UPCOMING;
        } else {
            return false;
        }

        String cacheKey = slot.chatId() + "_" + slot.startTime() + "_" + reminder;
        if (date.equals(notificationCache.get(cacheKey))) {
            return false;
        }

        try {
            TextMessage message = switch (reminder) {
                case STARTING -> entries.size() == 1
                        ? ClassMarkupUtil.createNowNotification(entries.getFirst(), chat, translationService)
                        : ClassMarkupUtil.createMultipleNowNotification(entries, chat, translationService);
                case UPCOMING -> entries.size() == 1
                        ? ClassMarkupUtil.createNextNotification(entries.getFirst(), chat, translationService)
                        : ClassMarkupUtil.createMultipleNextNotification(entries, chat, translationService);
            };
            notificationCache.put(cacheKey, date);
            telegramClient.sendTextMessage(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send the {} reminder for slot {} in chat {}",
                    reminder, slot.startTime(), slot.chatId(), e);
            return false;
        }
    }

    /**
     * Sends each opted-in member their own reminder, containing only the classes they attend. Members
     * who never started the bot cannot be messaged; Telegram rejects that outright, so they are
     * flagged rather than retried for every class.
     */
    private void remindMembers(SlotKey slot, List<ClassEntry> entries, LocalTime minute, LocalDate date) {
        LumiosChat chat = entries.getFirst().getDayEntry().getTimetableEntry().getChat();
        List<TimetableMember> members;
        try {
            members = personalTimetableService.remindableMembers(chat.getChatId());
        } catch (Exception e) {
            log.error("Could not load the members of chat {}", chat.getChatId(), e);
            return;
        }
        if (members.isEmpty()) {
            return;
        }

        long minutesAway = ChronoUnit.MINUTES.between(minute, slot.startTime());
        for (TimetableMember member : members) {
            long lead = member.getLeadMinutes() == null ? leadMinutes(chat) : clampLead(member.getLeadMinutes());
            if (minutesAway != 0 && minutesAway != lead) {
                continue;
            }

            String cacheKey = "dm_" + member.getTelegramUserId() + "_" + slot.chatId() + "_"
                              + slot.startTime() + "_" + (minutesAway == 0);
            if (date.equals(notificationCache.get(cacheKey))) {
                continue;
            }

            List<ClassEntry> mine;
            try {
                mine = personalTimetableSupport.personalClasses(chat.getChatId(), member.getTelegramUserId(), entries);
            } catch (Exception e) {
                log.error("Could not personalise the slot for member {}", member.getTelegramUserId(), e);
                continue;
            }
            if (mine.isEmpty()) {
                // Every class in this slot is an elective they do not take; nothing to say.
                notificationCache.put(cacheKey, date);
                continue;
            }

            notificationCache.put(cacheKey, date);
            TextMessage message = ClassMarkupUtil.createPersonalReminder(
                    mine, chat, member.getTelegramUserId(), minutesAway, translationService);
            if (telegramClient.sendTextMessage(message) == null) {
                log.info("Member {} is unreachable in private; muting their personal reminders",
                        member.getTelegramUserId());
                personalTimetableService.markDmUnavailable(chat.getChatId(), member.getTelegramUserId());
            }
        }
    }

    private int clampLead(int minutes) {
        return Math.clamp(minutes, 0, MAX_LEAD_MINUTES);
    }

    private int leadMinutes(LumiosChat chat) {
        Integer configured = chat.getReminderLeadMinutes();
        if (configured == null) {
            return 0;
        }
        return clampLead(configured);
    }

    @Scheduled(cron = "0 0 */6 * * *", zone = TimetableClock.ZONE_ID)
    public void cleanupNotificationCache() {
        LocalDate cutoff = TimetableClock.today().minusDays(1);
        int removed = 0;

        var iterator = notificationCache.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isBefore(cutoff)) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.info("Cleaned {} entries from notification cache", removed);
        }
    }

    private record SlotKey(Long chatId, LocalTime startTime) {
    }

    private enum Reminder {
        /**
         * Sent the moment the slot begins.
         */
        STARTING,
        /**
         * Sent {@code reminderLeadMinutes} before the slot begins.
         */
        UPCOMING
    }
}
