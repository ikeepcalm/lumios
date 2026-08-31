package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@BotCommand(command = "now", aliases = {"meow"})
public class NowCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService
                    .findByChatIdAndWeekType(message.getChatId(),
                            WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = TimetableClock.today().getDayOfWeek();
            LocalTime currentTime = TimetableClock.now();
            List<ClassEntry> currentClasses = new ArrayList<>();
            
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                        LocalTime startTime = classEntry.getStartTime();
                        LocalTime endTime = classEntry.getEndTime();
                        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                            currentClasses.add(classEntry);
                        }
                    }
                }
            }

            if (!currentClasses.isEmpty()) {
                if (currentClasses.size() == 1) {
                    sendMessage(ClassMarkupUtil.createNowNotification(currentClasses.getFirst(), chat, translationService), message);
                } else {
                    sendMessage(ClassMarkupUtil.createMultipleNowNotification(currentClasses, chat, translationService), message);
                }
            } else {
                sendMessage(translationService.getMessage("class.now.none", chat), message);
            }
        } catch (NoSuchEntityException e) {
            sendMessage(translationService.getMessage("class.now.error", chat), message);
        }
    }
}
