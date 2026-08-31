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
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.ClassMarkupUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@BotCommand(command = "next")
public class NextCommand extends ServicesShortcut implements Interaction {

    /**
     * How far ahead to look once today is over. A week is enough to cross an empty Friday/Saturday
     * and land on the other half of the two-week cycle.
     */
    private static final int LOOKAHEAD_DAYS = 7;

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        LocalDate today = TimetableClock.today();

        List<ClassEntry> remainingToday = classesAfter(chat, today, TimetableClock.now());
        if (!remainingToday.isEmpty()) {
            sendMessage(remainingToday.size() == 1
                    ? ClassMarkupUtil.createNextNotification(remainingToday.getFirst(), chat, translationService)
                    : ClassMarkupUtil.createMultipleNextNotification(remainingToday, chat, translationService), message);
            return;
        }

        // Nothing left today. Previously this branch sent no message at all, so /next just looked broken.
        for (int offset = 1; offset <= LOOKAHEAD_DAYS; offset++) {
            LocalDate date = today.plusDays(offset);
            List<ClassEntry> classes = classesAfter(chat, date, null);
            if (!classes.isEmpty()) {
                sendMessage(ClassMarkupUtil.createLaterDayNotification(
                        classes, date.getDayOfWeek(), chat, translationService), message);
                return;
            }
        }

        sendMessage(translationService.getMessage("class.next.none", chat), message);
    }

    /**
     * The earliest still-to-come slot on the given date, as the list of classes sharing that slot
     * (parallel electives all start together). Pass a null time to take the first slot of the day.
     *
     * @return the classes of that slot, or an empty list when the date has none
     */
    private List<ClassEntry> classesAfter(LumiosChat chat, LocalDate date, LocalTime after) {
        TimetableEntry timetable;
        try {
            timetable = timetableService.findByChatIdAndWeekType(chat.getChatId(), WeekValidator.determineWeekType(date));
        } catch (NoSuchEntityException e) {
            return List.of();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<ClassEntry> candidates = new ArrayList<>();
        for (DayEntry dayEntry : timetable.getDays()) {
            if (!dayOfWeek.equals(dayEntry.getDayName())) {
                continue;
            }
            for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                if (classEntry.getStartTime() != null && (after == null || classEntry.getStartTime().isAfter(after))) {
                    candidates.add(classEntry);
                }
            }
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        LocalTime earliest = candidates.stream()
                .map(ClassEntry::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        return candidates.stream()
                .filter(classEntry -> earliest.equals(classEntry.getStartTime()))
                .sorted(Comparator.comparing(classEntry -> classEntry.getName() == null ? "" : classEntry.getName()))
                .toList();
    }
}
