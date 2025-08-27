package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.ClassEntryRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ClassNotification {

    private final TelegramClient telegramClient;
    private final ClassEntryRepository classEntryRepository;
    
    private final Map<String, LocalDate> notificationCache = new ConcurrentHashMap<>();

    public ClassNotification(TelegramClient telegramClient, ClassEntryRepository classEntryRepository) {
        this.telegramClient = telegramClient;
        this.classEntryRepository = classEntryRepository;
    }

    @Scheduled(cron = "0 * 7-22 * * MON-FRI")
    public void checkForUpcomingClasses() {
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Kiev"));
        LocalTime nextMinute = now.plusMinutes(1);
        DayOfWeek today = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
        LocalDate currentDate = LocalDate.now(ZoneId.of("Europe/Kiev"));
        
        WeekType weekType = WeekValidator.determineWeekDay();
        
        try {
            List<ClassEntry> upcomingClasses = classEntryRepository.findUpcomingClasses(now, nextMinute, today);
            
            long processedCount = upcomingClasses.parallelStream()
                .filter(classEntry -> classEntry.getDayEntry().getTimetableEntry().getWeekType().equals(weekType))
                .filter(this::shouldSendNotification)
                .mapToLong(classEntry -> {
                    try {
                        LumiosChat chat = classEntry.getDayEntry().getTimetableEntry().getChat();
                        if (chat.isTimetableEnabled()) {
                            String cacheKey = generateCacheKey(classEntry, chat.getChatId());
                            notificationCache.put(cacheKey, currentDate);
                            
                            telegramClient.sendTextMessage(ClassMarkupUtil.createNowNotification(classEntry, chat.getChatId()));
                            return 1L;
                        }
                        return 0L;
                    } catch (Exception e) {
                        log.error("Failed to send class notification for class: {}", 
                                classEntry.getName(), e);
                        return 0L;
                    }
                })
                .sum();
                
            if (processedCount > 0) {
                log.info("Sent {} class notifications", processedCount);
            }
            
        } catch (Exception e) {
            log.error("Error during class notification check", e);
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanupNotificationCache() {
        LocalDate cutoff = LocalDate.now(ZoneId.of("Europe/Kiev")).minusDays(1);
        int removedCount = 0;
        
        var iterator = notificationCache.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isBefore(cutoff)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("Cleaned {} entries from notification cache", removedCount);
        }
    }

    private boolean shouldSendNotification(ClassEntry classEntry) {
        LumiosChat chat = classEntry.getDayEntry().getTimetableEntry().getChat();
        String cacheKey = generateCacheKey(classEntry, chat.getChatId());
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
        
        LocalDate lastNotified = notificationCache.get(cacheKey);
        return lastNotified == null || !lastNotified.equals(today);
    }

    private String generateCacheKey(ClassEntry classEntry, Long chatId) {
        return String.format("%d_%s_%s", 
            chatId, 
            classEntry.getId(),
            classEntry.getStartTime());
    }
}