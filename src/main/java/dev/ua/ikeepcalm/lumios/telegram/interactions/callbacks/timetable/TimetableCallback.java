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
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetablePagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;

/**
 * Paging for timetable keyboards sent before the personal views existed.
 * <p>
 * Kept so buttons already on screen at deploy time keep working. New keyboards carry the {@code view#}
 * family instead, handled by {@link TimetableViewCallback} - the {@code timetable-} payload splits on
 * {@code -} and so cannot carry the negative group chat id a private-chat view needs.
 */
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
                handleWeekNavigation(callbackQuery, timetableEntry, newPage, chat);
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

            String title = getTitleForCommandType(commandType, chat);
            String messageText = TimetablePagedUtil.buildPagedTimetableMessage(groupedByTime, newPage, title, translationService, chat);
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

    private void handleWeekNavigation(CallbackQuery callbackQuery, TimetableEntry timetableEntry, int newPage, LumiosChat chat) {
        List<DayEntry> daysWithClasses = timetableEntry.getDays().stream()
                .filter(day -> !day.getClassEntries().isEmpty())
                .sorted(Comparator.comparingInt(day -> day.getDayName().getValue()))
                .toList();

        if (newPage < 1 || newPage > daysWithClasses.size()) {
            return;
        }

        DayEntry dayEntry = daysWithClasses.get(newPage - 1);
        String messageText = TimetablePagedUtil.buildWeekDayMessage(
                dayEntry.getDayName(), dayEntry.getClassEntries(), newPage, daysWithClasses.size(), translationService, chat);

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
            // Through TimetableClock, not LocalDate.now(): the container has no TZ set, so a bare
            // call runs in UTC and turns the last page of the day over an hour early.
            case "today" -> {
                DayOfWeek today = TimetableClock.today().getDayOfWeek();
                yield getClassesForDay(timetableEntry, today);
            }
            case "tomorrow" -> {
                DayOfWeek tomorrow = TimetableClock.today().plusDays(1).getDayOfWeek();
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

    private String getTitleForCommandType(String commandType, LumiosChat chat) {
        return switch (commandType) {
            case "today" -> translationService.getMessage("timetable.title.today", chat);
            case "tomorrow" -> translationService.getMessage("timetable.title.tomorrow", chat);
            case "week" -> translationService.getMessage("timetable.title.week", chat);
            default -> translationService.getMessage("timetable.title.default", chat);
        };
    }
}
