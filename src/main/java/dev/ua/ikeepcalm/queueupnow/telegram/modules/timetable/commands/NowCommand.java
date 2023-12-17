package dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.utils.ClassMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.utils.WeekValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class NowCommand extends CommandParent {

    @Override
    @Transactional
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        try {
            TimetableEntry timetableEntry = timetableService
                    .findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();
            LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
            boolean isClassNow = false;
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                        LocalTime startTime = classEntry.getStartTime();
                        LocalTime endTime = classEntry.getEndTime();
                        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                            isClassNow = true;
                            sendMessage(ClassMarkupUtil.createNotification(classEntry, message.getChatId()));
                            break;
                        }
                    }
                }
            }

            if (!isClassNow) {
                sendMessage("Наразі жодної пари за розкладом не проходиь!");
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розкладу на даний момент! Ви точно все налаштували?");
        }
    }
}
