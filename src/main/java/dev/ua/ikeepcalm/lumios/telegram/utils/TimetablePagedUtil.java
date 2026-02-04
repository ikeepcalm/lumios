package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimetablePagedUtil {

    /**
     * Builds a paged timetable message showing one time slot per page
     *
     * @param groupedByTime Map of time slots to class entries (e.g., "08:30 - 10:00" -> [ClassEntry, ...])
     * @param page          Current page number (1-indexed)
     * @param title         Message title (e.g., "РОЗКЛАД НА СЬОГОДНІ")
     * @return Formatted message string
     */
    public static String buildPagedTimetableMessage(Map<String, List<ClassEntry>> groupedByTime, int page, String title) {
        List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());

        if (timeSlots.isEmpty()) {
            return "📅 *" + title + "* 📅\n\n🎆 *Немає пар!* 🎆";
        }

        int maxPage = timeSlots.size();
        if (page < 1 || page > maxPage) {
            page = 1;
        }

        String currentTimeSlot = timeSlots.get(page - 1);
        List<ClassEntry> classes = groupedByTime.get(currentTimeSlot);

        StringBuilder builder = new StringBuilder();
        builder.append("📅 *").append(title).append("* 📅\n\n");
        builder.append(TimetableParser.EMOJI_LEGEND);
        builder.append("⏰ *Слот ").append(page).append("/").append(maxPage).append(": ").append(currentTimeSlot).append("*\n\n");

        // Add class count indicator
        int lectureCount = 0, practiceCount = 0, labCount = 0;
        for (ClassEntry entry : classes) {
            switch (entry.getClassType().name()) {
                case "LECTURE" -> lectureCount++;
                case "PRACTICE" -> practiceCount++;
                case "LAB" -> labCount++;
            }
        }

        if (lectureCount > 0 || practiceCount > 0 || labCount > 0) {
            builder.append("📊 ");
            if (lectureCount > 0) builder.append(lectureCount).append(" лекц. ");
            if (practiceCount > 0) builder.append(practiceCount).append(" практ. ");
            if (labCount > 0) builder.append(labCount).append(" лаб.");
            builder.append("\n\n");
        }

        builder.append("_Натисніть на пару щоб перейти до посилання:_");

        return builder.toString();
    }

    /**
     * Builds inline keyboard with navigation buttons and class link buttons
     *
     * @param page        Current page number
     * @param maxPage     Total number of pages
     * @param classes     List of classes to show as buttons
     * @param commandType Type of command (today, tomorrow, week)
     * @return InlineKeyboardMarkup with navigation and class buttons
     */
    public static InlineKeyboardMarkup buildTimetableKeyboard(int page, int maxPage, List<ClassEntry> classes, String commandType) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        // Add class buttons (each class gets its own row for better readability)
        for (ClassEntry classEntry : classes) {
            String emoji = TimetableParser.parseClassEmoji(classEntry.getClassType());
            String buttonText = emoji + " " + truncateClassName(classEntry.getName());

            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton button = new InlineKeyboardButton(buttonText);

            if (classEntry.getUrl() != null && !classEntry.getUrl().isEmpty()) {
                // Class has URL - make it a clickable link
                button.setUrl(classEntry.getUrl());
            } else {
                // Class has no URL - make it a callback to add URL
                button.setCallbackData("classlink-add-" + classEntry.getId());
            }

            row.add(button);
            keyboard.add(row);
        }

        // Add navigation buttons if multiple pages
        if (maxPage > 1) {
            InlineKeyboardRow navRow = new InlineKeyboardRow();

            if (page > 1) {
                InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
                back.setCallbackData("timetable-" + commandType + "-" + page + "-back");
                navRow.add(back);
            }

            if (page < maxPage) {
                InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
                forward.setCallbackData("timetable-" + commandType + "-" + page + "-forward");
                navRow.add(forward);
            }

            keyboard.add(navRow);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Truncates long class names to fit in button text (max 64 chars for Telegram)
     */
    private static String truncateClassName(String name) {
        final int MAX_LENGTH = 55; // Leave room for emoji and spaces
        if (name.length() <= MAX_LENGTH) {
            return name;
        }
        return name.substring(0, MAX_LENGTH - 3) + "...";
    }

    /**
     * Extracts page number from callback data
     */
    public static int extractPage(String callbackData) {
        String[] parts = callbackData.split("-");
        if (parts.length >= 3) {
            return Integer.parseInt(parts[2]);
        }
        return 1;
    }

    /**
     * Extracts direction from callback data
     */
    public static String extractDirection(String callbackData) {
        String[] parts = callbackData.split("-");
        if (parts.length >= 4) {
            return parts[3];
        }
        return "forward";
    }

    /**
     * Extracts command type from callback data
     */
    public static String extractCommandType(String callbackData) {
        String[] parts = callbackData.split("-");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "today";
    }

    /**
     * Calculates the appropriate starting page based on current time.
     * Returns the page number of the current or next upcoming time slot.
     * If all classes are in the past, returns the last page.
     * If all classes are in the future, returns the first page.
     *
     * @param groupedByTime Map of time slots to class entries
     * @return Page number (1-indexed) to start from
     */
    public static int calculateCurrentPage(Map<String, List<ClassEntry>> groupedByTime) {
        if (groupedByTime.isEmpty()) {
            return 1;
        }

        LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
        List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());

        // Find the current or next time slot
        for (int i = 0; i < timeSlots.size(); i++) {
            String timeSlot = timeSlots.get(i);
            LocalTime endTime = parseEndTime(timeSlot);

            // If current time is before this slot ends, show this slot
            if (endTime != null && currentTime.isBefore(endTime)) {
                return i + 1; // Pages are 1-indexed
            }
        }

        // All slots are in the past, show the last one
        return timeSlots.size();
    }

    /**
     * Parses end time from a time slot string (e.g., "08:30 - 10:00" -> 10:00)
     */
    private static LocalTime parseEndTime(String timeSlot) {
        try {
            String[] parts = timeSlot.split(" - ");
            if (parts.length == 2) {
                return LocalTime.parse(parts[1].trim());
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }

    public static String getDayNameUkrainian(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Понеділок";
            case TUESDAY -> "Вівторок";
            case WEDNESDAY -> "Середа";
            case THURSDAY -> "Четвер";
            case FRIDAY -> "П'ятниця";
            case SATURDAY -> "Субота";
            case SUNDAY -> "Неділя";
        };
    }

    public static String buildPlainDayMessage(List<ClassEntry> classes, String title) {
        StringBuilder builder = new StringBuilder();
        builder.append("📅 *").append(title).append("* 📅\n\n");
        for (ClassEntry entry : classes) {
            builder.append(TimetableParser.parseClassEmoji(entry.getClassType()))
                    .append(" *").append(entry.getStartTime()).append(" - ").append(entry.getEndTime()).append("* ")
                    .append(entry.getName()).append("\n");
        }
        return builder.toString();
    }

    public static String buildWeekDayMessage(DayOfWeek dayOfWeek, List<ClassEntry> classes, int dayIndex, int totalDays) {
        StringBuilder builder = new StringBuilder();
        builder.append("📅 *РОЗКЛАД НА ТИЖДЕНЬ* 📅\n\n");
        builder.append("*").append(getDayNameUkrainian(dayOfWeek)).append("* (").append(dayIndex).append("/").append(totalDays).append(")\n\n");
        for (ClassEntry entry : classes) {
            builder.append(TimetableParser.parseClassEmoji(entry.getClassType()))
                    .append(" *").append(entry.getStartTime()).append(" - ").append(entry.getEndTime()).append("* ")
                    .append(entry.getName()).append("\n");
        }
        return builder.toString();
    }

    public static InlineKeyboardMarkup buildWeekDayKeyboard(int page, int totalPages) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        if (totalPages > 1) {
            InlineKeyboardRow navRow = new InlineKeyboardRow();
            if (page > 1) {
                InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
                back.setCallbackData("timetable-week-" + page + "-back");
                navRow.add(back);
            }
            if (page < totalPages) {
                InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
                forward.setCallbackData("timetable-week-" + page + "-forward");
                navRow.add(forward);
            }
            keyboard.add(navRow);
        }
        return new InlineKeyboardMarkup(keyboard);
    }
}
