package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns {@code /today}, {@code /tomorrow}, {@code /week}, {@code /now} and {@code /next} into personal
 * views.
 * <p>
 * Once a member has said which electives are theirs, a group timetable listing all six options in a
 * slot is answering somebody else's question. So the default is their own classes, with one tap - or the
 * argument {@code all} - to see everything. Members who have chosen nothing see no change at all.
 * <p>
 * These commands also work in a private chat, where the chat itself has no timetable: the group is
 * resolved from the member's own memberships instead.
 * <p>
 * Callback payloads, prefixed {@code view#} and split on {@code #} so a negative group chat id survives
 * intact - the older {@code timetable-} family splits on {@code -} and cannot carry one.
 * <ul>
 *     <li>{@code view#p#<chatId>#<command>#<page>#<scope>} - another page</li>
 *     <li>{@code view#s#<chatId>#<command>#<page>#<scope>} - switch to that scope</li>
 *     <li>{@code view#c#<chatId>#<command>} - which group to show, asked in a private chat</li>
 * </ul>
 */
@Component
public class TimetableViewSupport {

    public static final String PREFIX = "view#";
    public static final String PAGE = PREFIX + "p#";
    public static final String SWITCH = PREFIX + "s#";
    public static final String CHOOSE_CHAT = PREFIX + "c#";

    /**
     * Arguments that ask for the whole group timetable. The Ukrainian forms are here because the bot's
     * default language is Ukrainian and {@code /today все} is what a member would actually type.
     */
    private static final Set<String> ALL_ARGUMENTS = Set.of("all", "все", "всі", "усі");

    private final PersonalTimetableSupport personalTimetableSupport;
    private final TranslationService translationService;

    public TimetableViewSupport(PersonalTimetableSupport personalTimetableSupport,
                                TranslationService translationService) {
        this.personalTimetableSupport = personalTimetableSupport;
        this.translationService = translationService;
    }

    /**
     * Which timetable a command should read, and who is asking.
     *
     * @param groupChat the group whose timetable to show, or null when there is none to show
     * @param chooseFrom groups to offer the member when more than one could be meant
     */
    public record Target(LumiosChat groupChat, List<LumiosChat> chooseFrom) {

        public boolean resolved() {
            return groupChat != null;
        }

        public boolean ambiguous() {
            return groupChat == null && !chooseFrom.isEmpty();
        }
    }

    public enum Scope {
        MINE, ALL;

        public static Scope of(String token) {
            return "all".equalsIgnoreCase(token) ? ALL : MINE;
        }

        public Scope other() {
            return this == MINE ? ALL : MINE;
        }

        public String token() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * In a group the answer is the group itself. In a private chat there is no timetable to read, so the
     * member's own groups are consulted - one of which is usually the only candidate.
     */
    public Target target(Message message, LumiosChat chat, Long telegramUserId) {
        if (!"private".equals(message.getChat().getType())) {
            return new Target(chat, List.of());
        }

        List<LumiosChat> groups = personalTimetableSupport.timetabledGroupsOf(telegramUserId);
        if (groups.size() == 1) {
            return new Target(groups.getFirst(), List.of());
        }
        return new Target(null, groups);
    }

    /**
     * The scope the member asked for. Personal unless they wrote {@code all} after the command, and
     * unavoidably {@link Scope#ALL} for anyone who has chosen no electives - there is nothing to filter
     * by, and a "showing only yours" note would be a lie.
     */
    public Scope scopeFrom(Message message, Long groupChatId, Long telegramUserId) {
        if (!personalAvailable(groupChatId, telegramUserId)) {
            return Scope.ALL;
        }
        String text = message.getText() == null ? "" : message.getText().trim();
        int space = text.indexOf(' ');
        if (space > 0 && ALL_ARGUMENTS.contains(text.substring(space + 1).trim().toLowerCase(Locale.ROOT))) {
            return Scope.ALL;
        }
        return Scope.MINE;
    }

    public boolean personalAvailable(Long groupChatId, Long telegramUserId) {
        try {
            return personalTimetableSupport.personalAvailable(groupChatId, telegramUserId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The classes of one day the member actually attends, or all of them under {@link Scope#ALL}.
     */
    public List<ClassEntry> filterDay(Long groupChatId, Long telegramUserId, List<ClassEntry> dayClasses, Scope scope) {
        if (scope == Scope.ALL || dayClasses.isEmpty()) {
            return dayClasses;
        }
        return personalTimetableSupport.personalDay(groupChatId, telegramUserId, dayClasses);
    }

    /**
     * The line under a timetable saying whose it is. Nothing is added under {@link Scope#ALL} for a
     * member with no choices - they never asked a personal question.
     */
    public String footer(Scope scope, boolean personalAvailable, LumiosChat languageSource) {
        if (!personalAvailable) {
            return "";
        }
        return "\n\n" + translationService.getMessage(
                scope == Scope.MINE ? "view.footer.personal" : "view.footer.all", languageSource);
    }

    /**
     * The row offering the other scope, appended to a timetable keyboard. Absent for a member with no
     * choices, who has nothing to switch between.
     */
    public InlineKeyboardRow switchRow(Long groupChatId, String commandType, int page, Scope scope,
                                       boolean personalAvailable, LumiosChat languageSource) {
        if (!personalAvailable) {
            return null;
        }
        InlineKeyboardButton button = new InlineKeyboardButton(translationService.getMessage(
                scope == Scope.MINE ? "view.button.show_all" : "view.button.show_mine", languageSource));
        button.setCallbackData(SWITCH + groupChatId + "#" + commandType + "#" + page + "#" + scope.other().token());
        return new InlineKeyboardRow(button);
    }

    /**
     * Page navigation for the personal views, carrying the group and the scope so the callback can
     * rebuild the same view in a private chat.
     */
    public InlineKeyboardRow pageRow(Long groupChatId, String commandType, int page, int maxPage, Scope scope) {
        if (maxPage <= 1) {
            return null;
        }
        InlineKeyboardRow row = new InlineKeyboardRow();
        if (page > 1) {
            row.add(pageButton("⬅️", groupChatId, commandType, page - 1, scope));
        }
        if (page < maxPage) {
            row.add(pageButton("➡️", groupChatId, commandType, page + 1, scope));
        }
        return row.isEmpty() ? null : row;
    }

    /**
     * Which group did you mean? Asked only in a private chat, and only of members in more than one.
     */
    public InlineKeyboardMarkup chooseGroupKeyboard(List<LumiosChat> groups, String commandType) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>(groups.size());
        for (LumiosChat group : groups) {
            InlineKeyboardButton button = new InlineKeyboardButton(
                    group.getName() == null ? String.valueOf(group.getChatId()) : group.getName());
            button.setCallbackData(CHOOSE_CHAT + group.getChatId() + "#" + commandType);
            keyboard.add(new InlineKeyboardRow(button));
        }
        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Assembles a keyboard from the class buttons the shared builder produces plus the two rows only a
     * personal view needs.
     */
    public InlineKeyboardMarkup keyboard(List<InlineKeyboardRow> classButtons, Long groupChatId, String commandType,
                                         int page, int maxPage, Scope scope, boolean personalAvailable,
                                         LumiosChat languageSource) {
        List<InlineKeyboardRow> rows = new ArrayList<>(classButtons);
        InlineKeyboardRow pageRow = pageRow(groupChatId, commandType, page, maxPage, scope);
        if (pageRow != null) {
            rows.add(pageRow);
        }
        InlineKeyboardRow switchRow = switchRow(groupChatId, commandType, page, scope, personalAvailable, languageSource);
        if (switchRow != null) {
            rows.add(switchRow);
        }
        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardButton pageButton(String label, Long groupChatId, String commandType, int page, Scope scope) {
        InlineKeyboardButton button = new InlineKeyboardButton(label);
        button.setCallbackData(PAGE + groupChatId + "#" + commandType + "#" + page + "#" + scope.token());
        return button;
    }
}
