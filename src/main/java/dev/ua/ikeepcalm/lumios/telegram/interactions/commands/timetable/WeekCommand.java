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

@Component
@BotCommand(command = "week")
public class WeekCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());

            StringBuilder messageBuilder = new StringBuilder("📅 *РОЗКЛАД НА ТИЖДЕНЬ* 📅\n\n");
            messageBuilder.append(TimetableParser.EMOJI_LEGEND);
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (!dayEntry.getClassEntries().isEmpty()) {
                    messageBuilder.append("*{").append(dayEntry.getDayName()).append("}*\n");
                    messageBuilder.append(TimetableParser.formatClassEntriesGroupedByTime(dayEntry.getClassEntries()));
                }
            }

            sendMessage(messageBuilder.toString(), ParseMode.MARKDOWN, message);
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розклад на цей тиждень! Ви точно все налаштували?", message);
        }
    }

}
