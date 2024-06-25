package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
@BotCommand(command = "now")
public class NowCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
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
                            sendMessage(ClassMarkupUtil.createNowNotification(classEntry, message.getChatId()), message);
                            break;
                        }
                    }
                }
            }

            if (!isClassNow) {
                sendMessage("Наразі жодної пари за розкладом не проходить!", message);
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розкладу на даний момент! Ви точно все налаштували?", message);
        }
    }
}
