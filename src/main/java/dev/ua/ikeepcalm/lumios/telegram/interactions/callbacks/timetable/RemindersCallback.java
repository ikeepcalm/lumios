package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ReminderChannel;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.ReminderSettingsPicker;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.SettingsMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Handles the {@code rem#} family: every button of the {@code /reminders} menu. See
 * {@link ReminderSettingsPicker} for the payload contract.
 */
@Component
@BotCallback(startsWith = "rem#")
public class RemindersCallback extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(RemindersCallback.class);

    private final PersonalTimetableSupport support;

    public RemindersCallback(PersonalTimetableSupport support) {
        this.support = support;
    }

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] parts = callbackQuery.getData().split("#");
        String action = parts.length > 1 ? parts[1] : "";
        Long telegramUserId = callbackQuery.getFrom().getId();

        Long groupChatId = parseChatId(parts);
        LumiosChat groupChat = groupChatId == null ? null : support.chatOrNull(groupChatId);
        if (groupChat == null) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("mine.expired", chat), callbackQuery.getId());
            return;
        }

        switch (action) {
            case "g" -> openFromGroup(callbackQuery, groupChat, telegramUserId);
            case "c", "o" -> {
                telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
                support.sendReminderMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                        callbackQuery.getMessage().getMessageId());
            }
            case "ch" -> cycleChannel(callbackQuery, groupChat, telegramUserId);
            case "l" -> cycleLead(callbackQuery, groupChat, telegramUserId);
            case "dg" -> toggleDigest(callbackQuery, groupChat, telegramUserId);
            case "dt" -> cycleDigestTime(callbackQuery, groupChat, telegramUserId);
            case "m" -> {
                telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
                support.sendMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                        callbackQuery.getMessage().getMessageId());
            }
            case "d" -> done(callbackQuery, groupChat, telegramUserId);
            default -> {
                log.warn("Unrecognised reminder callback: {}", callbackQuery.getData());
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
        boolean delivered;
        try {
            delivered = support.sendReminderMenu(groupChat, telegramUserId, telegramUserId, null);
        } catch (Exception e) {
            log.info("Could not open the reminder menu privately for user {}: {}", telegramUserId, e.getMessage());
            delivered = false;
        }
        // Telegram refuses a private message outright rather than throwing here, so the toast has to be
        // driven by whether the send actually landed - not by the absence of an exception.
        telegramClient.sendAnswerCallbackQuery(translationService.getMessage(
                delivered ? "mine.sent-privately" : "mine.start-bot-first", groupChat), callbackQuery.getId());
    }

    private void cycleChannel(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        ReminderChannel next = ReminderSettingsPicker.nextChannel(member.getReminderChannel());
        member.setReminderChannel(next);
        // Asking for private messages again is also the member telling us they are reachable again.
        if (next.sendsDm()) {
            member.setDmUnavailable(false);
        }
        personalTimetableService.save(member);
        refresh(callbackQuery, groupChat, telegramUserId, null);
    }

    private void cycleLead(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        member.setLeadMinutes(SettingsMarkupUtil.nextLeadMinutes(
                ReminderSettingsPicker.effectiveLead(member, groupChat)));
        personalTimetableService.save(member);
        refresh(callbackQuery, groupChat, telegramUserId, null);
    }

    private void toggleDigest(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        member.setDigestEnabled(!member.isDigestEnabled());
        if (member.isDigestEnabled()) {
            member.setDmUnavailable(false);
            if (member.getDigestTime() == null) {
                member.setDigestTime(TimetableMember.DEFAULT_DIGEST_TIME);
            }
        }
        personalTimetableService.save(member);
        refresh(callbackQuery, groupChat, telegramUserId,
                member.isDigestEnabled() ? "reminders.toast.digest_on" : "reminders.toast.digest_off");
    }

    private void cycleDigestTime(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        member.setDigestTime(ReminderSettingsPicker.nextDigestTime(member.digestTimeOrDefault()));
        personalTimetableService.save(member);
        refresh(callbackQuery, groupChat, telegramUserId, null);
    }

    private void refresh(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId, String toastKey) {
        telegramClient.sendAnswerCallbackQuery(toastKey == null ? null : translationService.getMessage(
                toastKey, support.languageChatFor(telegramUserId, groupChat)), callbackQuery.getId());
        support.sendReminderMenu(groupChat, telegramUserId, callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private void done(CallbackQuery callbackQuery, LumiosChat groupChat, Long telegramUserId) {
        telegramClient.sendAnswerCallbackQuery(translationService.getMessage(
                "mine.toast.saved", support.languageChatFor(telegramUserId, groupChat)), callbackQuery.getId());
        RemoveMessage remove = new RemoveMessage();
        remove.setChatId(callbackQuery.getMessage().getChatId());
        remove.setMessageId(callbackQuery.getMessage().getMessageId());
        try {
            telegramClient.sendRemoveMessage(remove);
        } catch (TelegramApiException e) {
            log.debug("Could not remove the reminder menu", e);
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
