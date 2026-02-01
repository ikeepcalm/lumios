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
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetablePagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@BotCommand(command = "week")
public class WeekCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());

            // Collect all classes from all days
            List<ClassEntry> allWeekClasses = new ArrayList<>();
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (!dayEntry.getClassEntries().isEmpty()) {
                    allWeekClasses.addAll(dayEntry.getClassEntries());
                }
            }

            if (allWeekClasses.isEmpty()) {
                sendMessage("📅 *РОЗКЛАД НА ТИЖДЕНЬ* 📅\n\n🎆 *Немає пар на цей тиждень!* 🎆", ParseMode.MARKDOWN, message);
                return;
            }

            // Group classes by time slot
            Map<String, List<ClassEntry>> groupedByTime = TimetableParser.groupClassesByTime(allWeekClasses);

            // Build paged message and keyboard
            String messageText = TimetablePagedUtil.buildPagedTimetableMessage(groupedByTime, 1, "РОЗКЛАД НА ТИЖДЕНЬ");

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText(messageText);
            textMessage.setParseMode(ParseMode.MARKDOWN);

            // Add keyboard with class buttons and navigation
            List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());
            if (!timeSlots.isEmpty()) {
                List<ClassEntry> firstSlotClasses = groupedByTime.get(timeSlots.get(0));
                textMessage.setReplyKeyboard(TimetablePagedUtil.buildTimetableKeyboard(1, timeSlots.size(), firstSlotClasses, "week"));
            }

            sendMessage(textMessage, message);
        } catch (NoSuchEntityException e) {
            sendMessage("❌ Не знайдено розклад на цей тиждень! Ви точно все налаштували?", message);
        }
    }

}
