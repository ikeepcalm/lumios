package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.DayViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.SlotViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport.Scope;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


/**
 * Handles the {@code view#} family: paging, the personal/whole-group toggle, and the group chooser the
 * private-chat variants of these commands need. See {@link TimetableViewSupport} for the payload
 * contract.
 * <p>
 * Rendering is not duplicated here - each payload is handed straight back to the renderer the command
 * itself used, so a page turned by button looks exactly like a page shown by the command.
 */
@Component
@BotCallback(startsWith = "view#")
public class TimetableViewCallback extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(TimetableViewCallback.class);

    private final PersonalTimetableSupport support;
    private final DayViewRenderer dayRenderer;
    private final SlotViewRenderer slotRenderer;
    private final WeekViewRenderer weekRenderer;

    public TimetableViewCallback(PersonalTimetableSupport support, DayViewRenderer dayRenderer,
                                 SlotViewRenderer slotRenderer, WeekViewRenderer weekRenderer) {
        this.support = support;
        this.dayRenderer = dayRenderer;
        this.slotRenderer = slotRenderer;
        this.weekRenderer = weekRenderer;
    }

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] parts = callbackQuery.getData().split("#");
        String action = parts.length > 1 ? parts[1] : "";
        Long telegramUserId = callbackQuery.getFrom().getId();

        Long groupChatId = parseLong(parts, 2);
        LumiosChat groupChat = groupChatId == null ? null : support.chatOrNull(groupChatId);
        String commandType = parts.length > 3 ? parts[3] : "";
        if (groupChat == null || commandType.isEmpty()) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            return;
        }

        switch (action) {
            // Both the group chooser and the two navigation buttons end in the same place: render this
            // command, for this group, at this page and scope.
            case "c" -> render(callbackQuery, chat, groupChat, telegramUserId, commandType, 1, Scope.MINE);
            case "p", "s" -> {
                int page = parseInt(parts, 4, 1);
                Scope scope = Scope.of(parts.length > 5 ? parts[5] : "");
                render(callbackQuery, chat, groupChat, telegramUserId, commandType, page, scope);
            }
            default -> {
                log.warn("Unrecognised timetable view callback: {}", callbackQuery.getData());
                telegramClient.sendAnswerCallbackQuery(
                        translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            }
        }
    }

    private void render(CallbackQuery callbackQuery, LumiosChat chat, LumiosChat groupChat, Long telegramUserId,
                        String commandType, int page, Scope scope) {
        telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
        switch (commandType) {
            case "today" -> dayRenderer.replace(callbackQuery.getMessage(), chat, groupChat, telegramUserId,
                    commandType, TimetableClock.today(), page, scope);
            case "tomorrow" -> dayRenderer.replace(callbackQuery.getMessage(), chat, groupChat, telegramUserId,
                    commandType, TimetableClock.today().plusDays(1), page, scope);
            case "week" -> weekRenderer.replace(callbackQuery.getMessage(), chat, groupChat, telegramUserId,
                    page, scope);
            case "now", "next" -> slotRenderer.replace(callbackQuery.getMessage(), chat, groupChat,
                    telegramUserId, commandType, scope);
            default -> log.warn("Unrecognised timetable view command: {}", commandType);
        }
    }

    private Long parseLong(String[] parts, int index) {
        if (parts.length <= index) {
            return null;
        }
        try {
            return Long.parseLong(parts[index]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String[] parts, int index, int fallback) {
        if (parts.length <= index) {
            return fallback;
        }
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
