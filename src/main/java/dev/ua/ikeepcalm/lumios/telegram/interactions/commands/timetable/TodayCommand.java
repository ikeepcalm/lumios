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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            List<ClassEntry> todayClasses = new ArrayList<>();
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (dayEntry.getDayName().equals(dayOfWeek)) {
                    todayClasses = dayEntry.getClassEntries();
                    break;
                }
            }

            if (todayClasses.isEmpty()) {
                sendMessage(translationService.getMessage("command.today.no-classes", chat), ParseMode.MARKDOWN, message);
                return;
            }

            if (chat.isPlainTimetableEnabled()) {
                sendMessage(TimetablePagedUtil.buildPlainDayMessage(todayClasses, translationService.getMessage("command.today.title", chat)), ParseMode.MARKDOWN, message);
                return;
            }

            // Group classes by time slot
            Map<String, List<ClassEntry>> groupedByTime = TimetableParser.groupClassesByTime(todayClasses);

            // Calculate starting page based on current time
            int startPage = TimetablePagedUtil.calculateCurrentPage(groupedByTime);

            // Build paged message and keyboard
            String messageText = TimetablePagedUtil.buildPagedTimetableMessage(groupedByTime, startPage, translationService.getMessage("command.today.title", chat), translationService, chat);

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText(messageText);
            textMessage.setParseMode(ParseMode.MARKDOWN);

            // Add keyboard with class buttons and navigation
            List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());
            if (!timeSlots.isEmpty()) {
                List<ClassEntry> currentSlotClasses = groupedByTime.get(timeSlots.get(startPage - 1));
                textMessage.setReplyKeyboard(TimetablePagedUtil.buildTimetableKeyboard(startPage, timeSlots.size(), currentSlotClasses, "today"));
            }

            sendMessage(textMessage, message);
        } catch (NoSuchEntityException e) {
            sendMessage(translationService.getMessage("command.today.not-found", chat), message);
        }
    }


}
