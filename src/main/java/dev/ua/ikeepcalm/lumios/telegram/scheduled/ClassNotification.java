package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.ClassEntryRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class ClassNotification {

    private static final Logger log = LoggerFactory.getLogger(ClassNotification.class);
    private final TelegramClient telegramClient;
    private final ClassEntryRepository classEntryRepository;

    public ClassNotification(TelegramClient telegramClient, ClassEntryRepository classEntryRepository) {
        this.telegramClient = telegramClient;
        this.classEntryRepository = classEntryRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void checkForUpcomingClasses() {
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Kiev"));
        LocalTime nextMinute = now.plusMinutes(1);
        DayOfWeek today = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
        WeekType weekType = WeekValidator.determineWeekDay();
        List<ClassEntry> upcomingClasses = classEntryRepository.findUpcomingClasses(now, nextMinute, today);
        for (ClassEntry classEntry : upcomingClasses) {
            if (classEntry.getDayEntry().getTimetableEntry().getWeekType().equals(weekType)) {
                LumiosChat chat = classEntry.getDayEntry().getTimetableEntry().getChat();
                if (chat.isTimetableEnabled()) {
                    telegramClient.sendTextMessage(ClassMarkupUtil.createNowNotification(classEntry, chat.getChatId()));
                }
            }
        }
        log.debug("Checked for upcoming classes");
        if (upcomingClasses.isEmpty()) {
            log.debug("No upcoming classes");
        } else {
            log.info("Upcoming classes: {}", upcomingClasses);
        }
    }
}


