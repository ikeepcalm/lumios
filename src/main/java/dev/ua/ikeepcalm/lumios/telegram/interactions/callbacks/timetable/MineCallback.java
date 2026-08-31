package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@BotCallback(startsWith = "mine#")
public class MineCallback extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(MineCallback.class);

    private final PersonalTimetableSupport support;

    public MineCallback(PersonalTimetableSupport support) {
        this.support = support;
    }

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] parts = callbackQuery.getData().split("#");
        String action = parts.length > 1 ? parts[1] : "";
        Long telegramUserId = callbackQuery.getFrom().getId();

        Long groupChatId = parseChatId(parts);
        if (groupChatId == null) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            return;
        }

        LumiosChat groupChat = support.chatOrNull(groupChatId);
        if (groupChat == null) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            return;
        }

        switch (action) {
            case "g" -> openFromGroup(callbackQuery, groupChat, telegramUserId);
            case "c" -> {
                telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
                support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                        callbackQuery.getMessage().getMessageId());
            }
            case "p" -> page(callbackQuery, groupChat, telegramUserId, parts);
            case "t" -> toggle(callbackQuery, groupChat, telegramUserId, parts);
            case "r" -> toggleReminders(callbackQuery, groupChat, telegramUserId);
            case "d" -> done(callbackQuery, groupChat);
            default -> {
                log.warn("Unrecognised elective callback: {}", callbackQuery.getData());
                telegramClient.sendAnswerCallbackQuery(
                        translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            }
        }
    }

    /**
     * Tapped from the group. The menu is personal, so it is delivered privately - which only works if
     * the member has started the bot, and Telegram refuses outright otherwise.
     */
    private void openFromGroup(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        try {
            support.sendMenu(groupChat, telegramUserId, telegramUserId, null);
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.sent-privately", groupChat), callbackQuery.getId());
        } catch (Exception e) {
            log.info("Could not open the elective menu privately for user {}: {}", telegramUserId, e.getMessage());
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.start-bot-first", groupChat), callbackQuery.getId());
        }
    }

    private void page(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId, String[] parts) {
        int page = parts.length > 3 ? parseInt(parts[3], 0) : 0;
        telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
        support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(), page);
    }

    private void toggle(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId, String[] parts) {
        if (parts.length < 5) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.expired", groupChat), callbackQuery.getId());
            return;
        }

        int page = parseInt(parts[3], 0);
        String subjectKey = ElectiveDetector.resolveShortId(
                support.timetablesOf(groupChat.getChatId()), parts[4]);
        if (subjectKey == null) {
            // The timetable changed under the menu - most likely a re-import.
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.subject-gone", groupChat), callbackQuery.getId());
            support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId());
            return;
        }

        boolean nowAttending = personalTimetableService.toggleChoice(groupChat.getChatId(), telegramUserId, subjectKey);
        telegramClient.sendAnswerCallbackQuery(translationService.getMessage(
                nowAttending ? "mine.toast.added" : "mine.toast.removed", groupChat), callbackQuery.getId());

        support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(), page);
    }

    private void toggleReminders(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        member.setDmRemindersEnabled(!member.isDmRemindersEnabled());
        // Re-enabling is also the member telling us they are reachable again.
        if (member.isDmRemindersEnabled()) {
            member.setDmUnavailable(false);
        }
        personalTimetableService.save(member);

        telegramClient.sendAnswerCallbackQuery(translationService.getMessage(
                member.isDmRemindersEnabled() ? "mine.reminders.on" : "mine.reminders.off", groupChat),
                callbackQuery.getId());
        support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private void done(CallbackQuery callbackQuery, LumiosChat groupChat) {
        telegramClient.sendAnswerCallbackQuery(
                translationService.getMessage("mine.toast.saved", groupChat), callbackQuery.getId());
        RemoveMessage remove = new RemoveMessage();
        remove.setChatId(callbackQuery.getMessage().getChatId());
        remove.setMessageId(callbackQuery.getMessage().getMessageId());
        try {
            telegramClient.sendRemoveMessage(remove);
        } catch (TelegramApiException e) {
            log.debug("Could not remove the elective menu", e);
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Long parseChatId(String[] parts) {
        if (parts.length < 3) {
            return null;
        }
        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
