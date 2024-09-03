package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.moderation;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@BotCommand(command = "silence")
public class SilenceCommand extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(SilenceCommand.class);

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        if (message.getFrom().getUserName().equals("ikeepcalm")) {
            if (message.getReplyToMessage().hasText()) {
                message.getReplyToMessage().getFrom();
                RestrictChatMember restrictChatMember = new RestrictChatMember(String.valueOf(message.getChatId()), message.getReplyToMessage().getFrom().getId(), ChatPermissions.builder().canSendMessages(false).build(), 30, true);
                try {
                    telegramClient.execute(restrictChatMember);
                } catch (TelegramApiException e) {
                    log.error("Error while trying to silence user", e);
                }
            }
        }
    }
}