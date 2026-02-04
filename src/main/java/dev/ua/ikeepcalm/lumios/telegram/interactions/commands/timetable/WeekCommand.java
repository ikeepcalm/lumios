package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetablePagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Comparator;
import java.util.List;

@Component
@BotCommand(command = "week")
public class WeekCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());

            List<DayEntry> daysWithClasses = timetableEntry.getDays().stream()
                    .filter(day -> !day.getClassEntries().isEmpty())
                    .sorted(Comparator.comparingInt(day -> day.getDayName().getValue()))
                    .toList();

            if (daysWithClasses.isEmpty()) {
                sendMessage("📅 *РОЗКЛАД НА ТИЖДЕНЬ* 📅\n\n🎆 *Немає пар на цей тиждень!* 🎆", ParseMode.MARKDOWN, message);
                return;
            }

            DayEntry firstDay = daysWithClasses.get(0);
            String messageText = TimetablePagedUtil.buildWeekDayMessage(
                    firstDay.getDayName(), firstDay.getClassEntries(), 1, daysWithClasses.size());

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText(messageText);
            textMessage.setParseMode(ParseMode.MARKDOWN);
            textMessage.setReplyKeyboard(TimetablePagedUtil.buildWeekDayKeyboard(1, daysWithClasses.size()));

            sendMessage(textMessage, message);
        } catch (NoSuchEntityException e) {
            sendMessage("❌ Не знайдено розклад на цей тиждень! Ви точно все налаштували?", message);
        }
    }

}
