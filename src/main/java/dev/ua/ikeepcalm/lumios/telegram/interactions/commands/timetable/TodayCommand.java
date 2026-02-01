package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@BotCommand(command = "today")
public class TodayCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());
            DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

            StringBuilder messageBuilder = new StringBuilder("📅 *РОЗКЛАД НА СЬОГОДНІ* 📅\n\n");
            messageBuilder.append(TimetableParser.EMOJI_LEGEND);

            boolean hasClasses = false;

            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    if (!dayEntry.getClassEntries().isEmpty()) {
                        messageBuilder.append("*{").append(dayOfWeek.toString()).append("}*\n\n");
                        hasClasses = true;
                        messageBuilder.append(TimetableParser.formatClassEntriesGroupedByTime(dayEntry.getClassEntries()));
                    }
                }
            }

            if (!hasClasses) {
                messageBuilder.append("🎆 *Немає пар на сьогодні!* 🎆\n");
            }
            sendMessage(messageBuilder.toString(), ParseMode.MARKDOWN, message);
        } catch (NoSuchEntityException e) {
            sendMessage("❌ Не знайдено розклад на сьогодні! Ви точно все налаштували?", message);
        }
    }


}
