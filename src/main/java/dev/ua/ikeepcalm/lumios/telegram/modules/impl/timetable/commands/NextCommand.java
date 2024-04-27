package dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.commands;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class NextCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(), WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
            LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
            boolean isClassNow = false;
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                        LocalTime startTime = classEntry.getStartTime();
                        LocalTime endTime = classEntry.getEndTime();
                        if (isClassNow) {
                            sendMessage(ClassMarkupUtil.createNextNotification(classEntry, message.getChatId()));
                            isClassNow = false;
                            break;
                        } else if (currentTime.isBefore(startTime)) {
                            sendMessage(ClassMarkupUtil.createNextNotification(classEntry, message.getChatId()));
                            break;
                        }

                        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                            isClassNow = true;
                        }
                    }
                }
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розкладу на даний момент! Ви точно все налаштували?");
        }
    }
}
