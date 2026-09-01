package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectivePicker;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetablePagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassMarkupUtil {

    private static final String FALLBACK_SCHEDULE_URL = "https://ficeadvisor.com/schedule?week=1";

    public static TextMessage createNowNotification(ClassEntry classEntry, LumiosChat chat, TranslationService translationService) {
        return single(classEntry, chat, translationService,
                translationService.getMessage("class.now.notification", chat,
                        determineEmoji(classEntry.getClassType()), classEntry.getName()));
    }

    public static TextMessage createNextNotification(ClassEntry classEntry, LumiosChat chat, TranslationService translationService) {
        return single(classEntry, chat, translationService,
                translationService.getMessage("class.next.notification", chat,
                        classEntry.getStartTime(), determineEmoji(classEntry.getClassType()), classEntry.getName()));
    }

    public static TextMessage createMultipleNowNotification(List<ClassEntry> classEntries, LumiosChat chat, TranslationService translationService) {
        return createMultipleNowNotification(classEntries, chat, translationService, Map.of());
    }

    /**
     * @param tags mentions to hang off each class line, keyed by choice key - see
     *             {@code PersonalTimetableSupport#tagsBySubject}. Empty for a command response; only
     *             the scheduled announcement tags anybody.
     */
    public static TextMessage createMultipleNowNotification(List<ClassEntry> classEntries, LumiosChat chat,
                                                            TranslationService translationService,
                                                            Map<String, List<String>> tags) {
        return multiple(classEntries, chat, translationService,
                translationService.getMessage("class.multiple.now.notification", chat,
                        listClasses(classEntries, tags)));
    }

    public static TextMessage createMultipleNextNotification(List<ClassEntry> classEntries, LumiosChat chat, TranslationService translationService) {
        return createMultipleNextNotification(classEntries, chat, translationService, Map.of());
    }

    public static TextMessage createMultipleNextNotification(List<ClassEntry> classEntries, LumiosChat chat,
                                                             TranslationService translationService,
                                                             Map<String, List<String>> tags) {
        return multiple(classEntries, chat, translationService,
                translationService.getMessage("class.multiple.next.notification", chat,
                        classEntries.getFirst().getStartTime(), listClasses(classEntries, tags)));
    }

    /**
     * Used by /next once today is over: the next classes fall on a later day, so the day has to be
     * named - a bare start time would read as if they were still today.
     */
    public static TextMessage createLaterDayNotification(List<ClassEntry> classEntries, DayOfWeek day,
                                                        LumiosChat chat, TranslationService translationService) {
        return multiple(classEntries, chat, translationService,
                translationService.getMessage("class.next.later", chat,
                        TimetablePagedUtil.getDayName(day, translationService, chat),
                        classEntries.getFirst().getStartTime(),
                        listClasses(classEntries, Map.of())));
    }

    /**
     * One line per class, each carrying the mentions of the members who asked to be tagged on it. The
     * mention sits on the class line rather than in a block at the end, so the message says who has
     * which elective instead of just who is in the room.
     */
    private static String listClasses(List<ClassEntry> classEntries, Map<String, List<String>> tags) {
        boolean subgroup = ElectiveDetector.shapeOf(classEntries).subgroup();
        StringBuilder lines = new StringBuilder();
        for (ClassEntry classEntry : classEntries) {
            if (!lines.isEmpty()) {
                lines.append("\n");
            }
            lines.append(determineEmoji(classEntry.getClassType())).append(" ").append(classEntry.getName());
            List<String> mentions = tags.get(ElectiveDetector.choiceKey(classEntry, subgroup));
            if (mentions != null && !mentions.isEmpty()) {
                lines.append(" — ").append(String.join(" ", mentions));
            }
        }
        return lines.toString();
    }

    /**
     * One class: a single join button, plus a row to attach or clear its conference link.
     */
    private static TextMessage single(ClassEntry classEntry, LumiosChat chat, TranslationService translationService, String text) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardButton join = new InlineKeyboardButton(translationService.getMessage("class.button.link", chat));

        if (classEntry.getUrl() == null) {
            join.setText(translationService.getMessage("class.button.fice", chat));
            join.setUrl(FALLBACK_SCHEDULE_URL);
            keyboard.add(new InlineKeyboardRow(callbackButton(
                    translationService.getMessage("class.button.add_link", chat), "classlink-add-" + classEntry.getId())));
        } else {
            join.setUrl(classEntry.getUrl());
            keyboard.add(new InlineKeyboardRow(callbackButton(
                    translationService.getMessage("class.button.remove_link", chat), "classlink-remove-" + classEntry.getId())));
        }

        keyboard.add(new InlineKeyboardRow(join));
        return message(chat, text, keyboard);
    }

    /**
     * Several classes in one slot - typically parallel electives. Each gets its own join button, and
     * only "add link" is offered, since a remove button per class would double the keyboard.
     */
    private static TextMessage multiple(List<ClassEntry> classEntries, LumiosChat chat, TranslationService translationService, String text) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        for (ClassEntry classEntry : classEntries) {
            InlineKeyboardButton join;
            if (classEntry.getUrl() == null) {
                join = new InlineKeyboardButton(
                        translationService.getMessage("class.button.fice", chat) + " - " + classEntry.getName());
                join.setUrl(FALLBACK_SCHEDULE_URL);
            } else {
                join = new InlineKeyboardButton("🌐 " + classEntry.getName());
                join.setUrl(classEntry.getUrl());
            }
            keyboard.add(new InlineKeyboardRow(join));

            if (classEntry.getUrl() == null) {
                keyboard.add(new InlineKeyboardRow(callbackButton(
                        translationService.getMessage("class.button.add_link_for", chat, classEntry.getName()),
                        "classlink-add-" + classEntry.getId())));
            }
        }

        if (isElectivePool(classEntries) && chat.getChatId() != null && chat.getChatId() < 0) {
            keyboard.add(new InlineKeyboardRow(callbackButton(
                    translationService.getMessage("mine.button.open", chat),
                    ElectivePicker.FROM_GROUP + chat.getChatId())));
        }

        return message(chat, text, keyboard);
    }

    /**
     * A slot that offers a choice is worth inviting readers to say which half is theirs.
     */
    private static boolean isElectivePool(List<ClassEntry> classEntries) {
        return ElectiveDetector.shapeOf(classEntries).offersChoice();
    }

    /**
     * A member's own reminder, sent to their private chat and listing only the classes they attend.
     * Carries the teacher, which the group message has no room for.
     *
     * @param minutesAway 0 when the slot is starting now
     */
    public static TextMessage createPersonalReminder(List<ClassEntry> classEntries, LumiosChat groupChat,
                                                     long dmChatId, long minutesAway,
                                                     TranslationService translationService) {
        StringBuilder lines = new StringBuilder();
        for (ClassEntry classEntry : classEntries) {
            lines.append(determineEmoji(classEntry.getClassType())).append(" ").append(classEntry.getName());
            String detail = detailLine(classEntry);
            if (!detail.isEmpty()) {
                lines.append("\n     ").append(detail);
            }
            lines.append("\n");
        }

        String text = minutesAway <= 0
                ? translationService.getMessage("class.personal.now", groupChat,
                        classEntries.getFirst().getStartTime(), lines.toString().trim())
                : translationService.getMessage("class.personal.soon", groupChat,
                        String.valueOf(minutesAway), classEntries.getFirst().getStartTime(), lines.toString().trim());

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (ClassEntry classEntry : classEntries) {
            if (classEntry.getUrl() != null) {
                InlineKeyboardButton join = new InlineKeyboardButton("🌐 " + classEntry.getName());
                join.setUrl(classEntry.getUrl());
                keyboard.add(new InlineKeyboardRow(join));
            }
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(dmChatId);
        textMessage.setText(text);
        if (!keyboard.isEmpty()) {
            textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        }
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    /**
     * The member's whole day in one private message, sent once each morning.
     * <p>
     * The per-class reminders are the noisy part of the feature; a member who wants to know what is
     * coming without being interrupted five times gets this instead, or as well.
     *
     * @param classEntries every class the member attends today, in any order
     */
    public static TextMessage createDigest(List<ClassEntry> classEntries, LumiosChat languageSource,
                                           long dmChatId, TranslationService translationService) {
        Map<LocalTime, List<ClassEntry>> bySlot = new LinkedHashMap<>();
        classEntries.stream()
                .sorted(Comparator.comparing(ClassEntry::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(classEntry -> bySlot
                        .computeIfAbsent(classEntry.getStartTime(), time -> new ArrayList<>())
                        .add(classEntry));

        StringBuilder lines = new StringBuilder();
        bySlot.forEach((startTime, slot) -> {
            lines.append("\n*").append(startTime).append("*\n");
            for (ClassEntry classEntry : slot) {
                lines.append(determineEmoji(classEntry.getClassType())).append(" ").append(classEntry.getName());
                String detail = detailLine(classEntry);
                if (!detail.isEmpty()) {
                    lines.append("\n     ").append(detail);
                }
                lines.append("\n");
            }
        });

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(dmChatId);
        textMessage.setText(translationService.getMessage("digest.title", languageSource,
                String.valueOf(classEntries.size())) + "\n" + lines.toString().stripTrailing());
        textMessage.setParseMode(ParseMode.MARKDOWN);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (ClassEntry classEntry : classEntries) {
            if (classEntry.getUrl() != null) {
                InlineKeyboardButton join = new InlineKeyboardButton("🌐 " + classEntry.getName());
                join.setUrl(classEntry.getUrl());
                keyboard.add(new InlineKeyboardRow(join));
            }
        }
        if (!keyboard.isEmpty()) {
            textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        }
        return textMessage;
    }

    private static String detailLine(ClassEntry classEntry) {
        List<String> parts = new ArrayList<>(2);
        if (classEntry.getTeacherName() != null && !classEntry.getTeacherName().isBlank()) {
            parts.add(classEntry.getTeacherName());
        }
        if (classEntry.getLocation() != null && !classEntry.getLocation().isBlank()) {
            parts.add(classEntry.getLocation());
        }
        return String.join(" · ", parts);
    }

    private static InlineKeyboardButton callbackButton(String label, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(label);
        button.setCallbackData(callbackData);
        return button;
    }

    private static TextMessage message(LumiosChat chat, String text, List<InlineKeyboardRow> keyboard) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());
        textMessage.setText(text);
        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    private static String determineEmoji(ClassType classType) {
        return switch (classType.name()) {
            case "LECTURE" -> "🔵";
            case "PRACTICE" -> "🟠";
            case "LAB" -> "🟢";
            default -> "?";
        };
    }

}
