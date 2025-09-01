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
@BotCommand(command = "next")
public class NextCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(), WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
            LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
            boolean isClassNow = false;
            LocalTime nextStartTime = null;
            List<ClassEntry> nextClasses = new ArrayList<>();
            
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                        LocalTime startTime = classEntry.getStartTime();
                        LocalTime endTime = classEntry.getEndTime();
                        
                        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                            isClassNow = true;
                        }
                        
                        if (isClassNow && currentTime.isBefore(startTime)) {
                            if (nextStartTime == null || startTime.equals(nextStartTime)) {
                                nextStartTime = startTime;
                                nextClasses.add(classEntry);
                            } else if (startTime.isBefore(nextStartTime)) {
                                nextStartTime = startTime;
                                nextClasses.clear();
                                nextClasses.add(classEntry);
                            }
                        } else if (!isClassNow && currentTime.isBefore(startTime)) {
                            if (nextStartTime == null || startTime.equals(nextStartTime)) {
                                nextStartTime = startTime;
                                nextClasses.add(classEntry);
                            } else if (startTime.isBefore(nextStartTime)) {
                                nextStartTime = startTime;
                                nextClasses.clear();
                                nextClasses.add(classEntry);
                            }
                        }
                    }
                }
            }
            
            if (!nextClasses.isEmpty()) {
                if (nextClasses.size() == 1) {
                    sendMessage(ClassMarkupUtil.createNextNotification(nextClasses.getFirst(), message.getChatId()), message);
                } else {
                    sendMessage(ClassMarkupUtil.createMultipleNextNotification(nextClasses, message.getChatId()), message);
                }
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розкладу на даний момент! Ви точно все налаштували?", message);
        }
    }
}
