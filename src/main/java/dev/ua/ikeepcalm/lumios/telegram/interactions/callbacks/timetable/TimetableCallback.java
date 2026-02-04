package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetablePagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;

@Component
@BotCallback(startsWith = "timetable")
public class TimetableCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String callbackData = callbackQuery.getData();

        // Extract data from callback
        String commandType = TimetablePagedUtil.extractCommandType(callbackData);
        int currentPage = TimetablePagedUtil.extractPage(callbackData);
        String direction = TimetablePagedUtil.extractDirection(callbackData);

        // Calculate new page
        int newPage = direction.equals("forward") ? currentPage + 1 : currentPage - 1;

        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(
                    callbackQuery.getMessage().getChatId(),
                    WeekValidator.determineWeekDay()
            );

            if ("week".equals(commandType)) {
                handleWeekNavigation(callbackQuery, timetableEntry, newPage);
                return;
            }

            List<ClassEntry> classes = getClassesForCommandType(timetableEntry, commandType);
            if (classes.isEmpty()) {
                return;
            }

            Map<String, List<ClassEntry>> groupedByTime = TimetableParser.groupClassesByTime(classes);
            List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());
            int maxPage = timeSlots.size();
            if (newPage < 1 || newPage > maxPage) {
                return;
            }

            String title = getTitleForCommandType(commandType);
            String messageText = TimetablePagedUtil.buildPagedTimetableMessage(groupedByTime, newPage, title);
            List<ClassEntry> pageClasses = groupedByTime.get(timeSlots.get(newPage - 1));

            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(callbackQuery.getMessage().getChatId());
            editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessage.setText(messageText);
            editMessage.setParseMode(ParseMode.MARKDOWN);
            editMessage.setReplyKeyboard(TimetablePagedUtil.buildTimetableKeyboard(newPage, maxPage, pageClasses, commandType));

            editMessage(editMessage);

        } catch (NoSuchEntityException e) {
            // Ignore - timetable not found
        }
    }

    private void handleWeekNavigation(CallbackQuery callbackQuery, TimetableEntry timetableEntry, int newPage) {
        List<DayEntry> daysWithClasses = timetableEntry.getDays().stream()
                .filter(day -> !day.getClassEntries().isEmpty())
                .sorted(Comparator.comparingInt(day -> day.getDayName().getValue()))
                .toList();

        if (newPage < 1 || newPage > daysWithClasses.size()) {
            return;
        }

        DayEntry dayEntry = daysWithClasses.get(newPage - 1);
        String messageText = TimetablePagedUtil.buildWeekDayMessage(
                dayEntry.getDayName(), dayEntry.getClassEntries(), newPage, daysWithClasses.size());

        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(callbackQuery.getMessage().getChatId());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText(messageText);
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage.setReplyKeyboard(TimetablePagedUtil.buildWeekDayKeyboard(newPage, daysWithClasses.size()));

        editMessage(editMessage);
    }

    private List<ClassEntry> getClassesForCommandType(TimetableEntry timetableEntry, String commandType) {
        return switch (commandType) {
            case "today" -> {
                DayOfWeek today = LocalDate.now().getDayOfWeek();
                yield getClassesForDay(timetableEntry, today);
            }
            case "tomorrow" -> {
                DayOfWeek tomorrow = LocalDate.now().plusDays(1).getDayOfWeek();
                yield getClassesForDay(timetableEntry, tomorrow);
            }
            default -> new ArrayList<>();
        };
    }

    private List<ClassEntry> getClassesForDay(TimetableEntry timetableEntry, DayOfWeek dayOfWeek) {
        for (DayEntry dayEntry : timetableEntry.getDays()) {
            if (dayEntry.getDayName().equals(dayOfWeek)) {
                return dayEntry.getClassEntries();
            }
        }
        return new ArrayList<>();
    }

    private String getTitleForCommandType(String commandType) {
        return switch (commandType) {
            case "today" -> "РОЗКЛАД НА СЬОГОДНІ";
            case "tomorrow" -> "РОЗКЛАД НА ЗАВТРА";
            case "week" -> "РОЗКЛАД НА ТИЖДЕНЬ";
            default -> "РОЗКЛАД";
        };
    }
}
