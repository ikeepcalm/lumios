package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component
@BotCallback(endsWith = "simple-delete")
public class DeleteCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-simple-delete", "");
        for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
            if (chatMember.getUser().getId().equals(message.getFrom().getId()) || message.getFrom().getUserName().equals("ikeepcalm")) {
                SimpleQueue simpleQueue;
                try {
                    simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
                    queueService.deleteSimpleQueue(simpleQueue);
                    TextMessage textMessage = new TextMessage();
                    textMessage.setChatId(message.getMessage().getChatId());
                    textMessage.setText("@".concat(message.getFrom().getUserName()).concat(" видалив чергу: ").concat(simpleQueue.getAlias()).concat("!"));
                    sendMessage(textMessage, (Message) message.getMessage());
                    telegramClient.sendRemoveMessage(new RemoveMessage(simpleQueue.getMessageId(), message.getMessage().getChatId()));

                    break;
                } catch (NoSuchEntityException e) {
                    sendMessage("Помилка! Не знайдено чергу з таким ID!", (Message) message.getMessage());
                }
            }
        }
    }
}

