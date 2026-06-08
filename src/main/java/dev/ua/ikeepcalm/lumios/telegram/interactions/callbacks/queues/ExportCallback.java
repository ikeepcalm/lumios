package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component
@BotCallback(endsWith = "simple-export")
public class ExportCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-simple-export", "");
        String callbackQueryId = message.getId();
        SimpleQueue simpleQueue;
        try {
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));

            telegramClient.sendAnswerCallbackQuery(translationService.getMessage("queue.export.success", chat), callbackQueryId);

            Message holdMessage = sendMessage(translationService.getMessage("queue.export.loading", chat), (Message) message.getMessage());

            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(holdMessage.getChatId());
            editMessage.setMessageId(holdMessage.getMessageId());
            editMessage.setText(createExport(chat, simpleQueue));
            editMessage.setParseMode(ParseMode.MARKDOWN);

            telegramClient.sendEditMessage(editMessage);

        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery(translationService.getMessage("queue.error.not_found", chat), callbackQueryId);
        }

    }

    public String createExport(LumiosChat chat, SimpleQueue simpleQueue) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(translationService.getMessage("queue.export.title", chat, simpleQueue.getAlias()));

        int i = 1;
        for (SimpleUser simpleUser : simpleQueue.getContents()) {
            LumiosUser user;
            try {
                user = userService.findById(simpleUser.getAccountId(), chat);
            } catch (Exception e) {
                user = null;
            }

            if (user != null) {
                String fullName = user.getFullName();
                if (fullName == null) {
                    fullName = "[" + simpleUser.getUsername() + "]" + "(" + "tg://user?id=" + simpleUser.getAccountId() + ") - " + translationService.getMessage("queue.export.identity_hint", chat);
                }

                stringBuilder.append(i++).append(") - ").append(fullName).append("\n");
            }
        }

        return stringBuilder.toString();
    }
}
