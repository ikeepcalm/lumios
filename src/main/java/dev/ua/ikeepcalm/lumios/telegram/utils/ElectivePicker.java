package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector.Elective;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The private-chat menu where a member marks which electives they attend.
 * <p>
 * Callback payloads, all prefixed {@code mine#} so one handler receives them. Every payload carries
 * the group's chat id, because the menu itself lives in a private chat and the callback would
 * otherwise arrive with no idea which timetable it refers to.
 * <ul>
 *     <li>{@code mine#t#<chatId>#<page>#<shortId>} - toggle one elective, staying on that page</li>
 *     <li>{@code mine#p#<chatId>#<page>} - another page of electives</li>
 *     <li>{@code mine#r#<chatId>} - turn personal reminders on or off</li>
 *     <li>{@code mine#d#<chatId>} - done, close the menu</li>
 *     <li>{@code mine#g#<chatId>} - sent from a group: deliver this menu privately</li>
 *     <li>{@code mine#c#<chatId>} - pick which group to configure</li>
 * </ul>
 */
public final class ElectivePicker {

    public static final String PREFIX = "mine#";
    public static final String TOGGLE = PREFIX + "t#";
    public static final String PAGE = PREFIX + "p#";
    public static final String REMINDERS = PREFIX + "r#";
    public static final String DONE = PREFIX + "d#";
    public static final String FROM_GROUP = PREFIX + "g#";
    public static final String CHOOSE_CHAT = PREFIX + "c#";

    private static final int PAGE_SIZE = 8;

    /**
     * Elective names run past 100 characters; Telegram would render the whole thing and swamp the
     * keyboard. The leading words are distinctive enough to recognise a subject by.
     */
    private static final int LABEL_LIMIT = 42;

    private ElectivePicker() {
    }

    public static int totalPages(int electiveCount) {
        return Math.max(1, (electiveCount + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public static int clampPage(int page, int electiveCount) {
        return Math.clamp(page, 0, totalPages(electiveCount) - 1);
    }

    public static String text(TranslationService translations, LumiosChat chat, String groupName,
                              List<Elective> electives, Set<String> chosen, boolean remindersEnabled, int page) {
        StringBuilder text = new StringBuilder();
        text.append(translations.getMessage("mine.title", chat, groupName));
        text.append("\n\n").append(translations.getMessage("mine.hint", chat));
        text.append("\n\n").append(translations.getMessage("mine.chosen", chat,
                String.valueOf(countChosen(electives, chosen)), String.valueOf(electives.size())));

        int pages = totalPages(electives.size());
        if (pages > 1) {
            text.append("\n").append(translations.getMessage("mine.page", chat,
                    String.valueOf(page + 1), String.valueOf(pages)));
        }
        text.append("\n\n").append(remindersEnabled
                ? translations.getMessage("mine.reminders.on", chat)
                : translations.getMessage("mine.reminders.off", chat));
        return text.toString();
    }

    public static InlineKeyboardMarkup keyboard(TranslationService translations, LumiosChat chat, Long groupChatId,
                                                List<Elective> electives, Set<String> chosen,
                                                boolean remindersEnabled, int page) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, electives.size());
        for (int i = from; i < to; i++) {
            Elective elective = electives.get(i);
            boolean picked = chosen.contains(elective.subjectKey());
            InlineKeyboardButton button = new InlineKeyboardButton(
                    (picked ? "✅ " : "▫️ ") + shorten(elective.name()));
            button.setCallbackData(TOGGLE + groupChatId + "#" + page + "#" + elective.shortId());
            keyboard.add(new InlineKeyboardRow(button));
        }

        InlineKeyboardRow navigation = new InlineKeyboardRow();
        if (page > 0) {
            navigation.add(button(translations.getMessage("command.import.button.prev", chat),
                    PAGE + groupChatId + "#" + (page - 1)));
        }
        if (to < electives.size()) {
            navigation.add(button(translations.getMessage("command.import.button.next", chat),
                    PAGE + groupChatId + "#" + (page + 1)));
        }
        if (!navigation.isEmpty()) {
            keyboard.add(navigation);
        }

        keyboard.add(new InlineKeyboardRow(button(remindersEnabled
                        ? translations.getMessage("mine.button.reminders_disable", chat)
                        : translations.getMessage("mine.button.reminders_enable", chat),
                REMINDERS + groupChatId)));
        keyboard.add(new InlineKeyboardRow(button(
                translations.getMessage("mine.button.done", chat), DONE + groupChatId)));

        return new InlineKeyboardMarkup(keyboard);
    }

    private static int countChosen(List<Elective> electives, Set<String> chosen) {
        int count = 0;
        for (Elective elective : electives) {
            if (chosen.contains(elective.subjectKey())) {
                count++;
            }
        }
        return count;
    }

    private static String shorten(String name) {
        if (name == null) {
            return "";
        }
        String collapsed = name.trim();
        return collapsed.length() <= LABEL_LIMIT ? collapsed : collapsed.substring(0, LABEL_LIMIT - 1).trim() + "…";
    }

    private static InlineKeyboardButton button(String label, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(label);
        button.setCallbackData(callbackData);
        return button;
    }
}
