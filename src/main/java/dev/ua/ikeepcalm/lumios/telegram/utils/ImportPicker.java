package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil.CampusGroup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the group picker of {@code /import}: the text above the keyboard and the keyboard itself.
 * Shared by the command (first page) and the callback (page navigation) so both always look the same.
 * <p>
 * Callback payloads, all prefixed with {@code import#} so a single handler receives them:
 * <ul>
 *     <li>{@code import#g#<groupId>} - import this group</li>
 *     <li>{@code import#p#<page>#<query>} - show another page of results</li>
 *     <li>{@code import#x} - dismiss the picker</li>
 * </ul>
 */
public final class ImportPicker {

    public static final String PICK_PREFIX = "import#g#";
    public static final String PAGE_PREFIX = "import#p#";
    public static final String CANCEL = "import#x";

    private static final int PAGE_SIZE = 8;
    private static final int COLUMNS = 2;

    private ImportPicker() {
    }

    public static int totalPages(int totalGroups) {
        return Math.max(1, (totalGroups + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public static int clampPage(int page, int totalGroups) {
        return Math.clamp(page, 0, totalPages(totalGroups) - 1);
    }

    public static String text(TranslationService translations, LumiosChat chat, String query,
                              List<CampusGroup> groups, int page) {
        StringBuilder text = new StringBuilder();
        if (groups.size() == 1) {
            CampusGroup only = groups.getFirst();
            text.append(translations.getMessage("command.import.picker.single", chat, only.name(),
                    only.faculty() == null ? "—" : only.faculty()));
        } else {
            text.append(translations.getMessage("command.import.picker.header", chat, String.valueOf(groups.size()), query));
            int pages = totalPages(groups.size());
            if (pages > 1) {
                text.append("\n").append(translations.getMessage("command.import.picker.page", chat, String.valueOf(page + 1), String.valueOf(pages)));
            }
        }
        text.append("\n\n").append(translations.getMessage("command.import.picker.hint", chat));
        return text.toString();
    }

    public static InlineKeyboardMarkup keyboard(TranslationService translations, LumiosChat chat, String query,
                                                List<CampusGroup> groups, int page) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, groups.size());
        InlineKeyboardRow row = new InlineKeyboardRow();
        for (int i = from; i < to; i++) {
            CampusGroup group = groups.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton(group.label());
            button.setCallbackData(PICK_PREFIX + group.id());
            row.add(button);
            if (row.size() == COLUMNS) {
                keyboard.add(row);
                row = new InlineKeyboardRow();
            }
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        InlineKeyboardRow navigation = new InlineKeyboardRow();
        if (page > 0) {
            navigation.add(pageButton(translations.getMessage("command.import.button.prev", chat), page - 1, query));
        }
        if (to < groups.size()) {
            navigation.add(pageButton(translations.getMessage("command.import.button.next", chat), page + 1, query));
        }
        if (!navigation.isEmpty()) {
            keyboard.add(navigation);
        }

        InlineKeyboardButton cancel = new InlineKeyboardButton(translations.getMessage("command.import.button.cancel", chat));
        cancel.setCallbackData(CANCEL);
        keyboard.add(new InlineKeyboardRow(cancel));

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardButton pageButton(String label, int page, String query) {
        InlineKeyboardButton button = new InlineKeyboardButton(label);
        button.setCallbackData(PAGE_PREFIX + page + "#" + query);
        return button;
    }
}
