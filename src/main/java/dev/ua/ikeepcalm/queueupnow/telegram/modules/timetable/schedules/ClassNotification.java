package dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.schedules;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.timetable.ClassEntryRepository;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.AbsSender;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.utils.ClassMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.utils.WeekValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ClassNotification {
    private final ChatService chatService;
    private final TimetableService timetableService;
    private final AbsSender absSender;
    private final List<ClassEntry> allClassEntries;
    private final Set<ClassEntry> sendNotifications;
    private final Logger logger = LoggerFactory.getLogger(ClassNotification.class);


    public ClassNotification(ChatService chatService,
                             TimetableService timetableService,
                             ClassEntryRepository classEntryRepository,
                             AbsSender absSender) {
        this.chatService = chatService;
        this.timetableService = timetableService;
        this.absSender = absSender;
        this.sendNotifications = new HashSet<>();
        this.allClassEntries = (List<ClassEntry>) classEntryRepository.findAll();
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void updateClassEntries(){
        DayOfWeek currentDay = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
        logger.info("Updating class entries for day: {}", currentDay);
        Iterable<ReverenceChat> reverenceChats = chatService.findAll();
        for (ReverenceChat reverenceChat : reverenceChats){
            TimetableEntry timetableEntry;
            try {
                timetableEntry = timetableService.findByChatIdAndWeekType(reverenceChat.getChatId(), WeekValidator.determineWeekDay());
                for (DayEntry dayEntry : timetableEntry.getDays()) {
                    if (dayEntry.getDayName().equals(currentDay)) {
                        allClassEntries.clear();
                        allClassEntries.addAll(dayEntry.getClassEntries());
                    }
                }
            } catch (NoSuchEntityException e) {
                logger.debug("Couldn't find Timetable for chat {} and week type {}", reverenceChat.getChatId(), WeekValidator.determineWeekDay());
            }
        } logger.debug("Updated class entries: {}", allClassEntries);
    }

    @Scheduled(cron = "0 0 0 * * * ")
    public void clearSentNotifications() {
        logger.info("Clearing sent notifications.");
        sendNotifications.clear();
        logger.debug("Sent notifications cleared.");
    }


    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void sendClassNotifications() {
        LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
        DayOfWeek currentDay = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();

        for (ClassEntry classEntry : allClassEntries) {
            if (classEntry.getDayEntry().getDayName() == currentDay &&
                    classEntry.getStartTime().isAfter(currentTime) &&
                    classEntry.getStartTime().isBefore(currentTime.plusMinutes(1)) &&
                    !sendNotifications.contains(classEntry)) {
                absSender.sendTextMessage(ClassMarkupUtil.createNotification(classEntry, classEntry.getDayEntry().getTimetableEntry().getChat().getChatId()));
                sendNotifications.add(classEntry);
            }
        }
        logger.debug("Notifications sent for class entries: {}", sendNotifications);
    }
}


