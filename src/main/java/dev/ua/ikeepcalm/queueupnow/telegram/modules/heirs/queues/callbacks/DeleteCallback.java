package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.queues.callbacks;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CallbackParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.UUID;

@Component
public class DeleteCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-delete", "");
        instantiateUpdate(message);
        for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
            if (chatMember.getUser().getId().equals(message.getFrom().getId()) || message.getFrom().getUserName().equals("ikeepcalm")) {
                SimpleQueue simpleQueue = null;
                try {
                    simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
                    queueService.deleteSimpleQueue(simpleQueue);
                    telegramClient.sendRemoveMessage(new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId()));
                    TextMessage textMessage = new TextMessage();
                    textMessage.setChatId(message.getMessage().getChatId());
                    textMessage.setText("@".concat(message.getFrom().getUserName()).concat(" видалив чергу: ").concat(simpleQueue.getAlias()).concat("!"));
                    sendMessage(textMessage);
                    break;
                } catch (NoSuchEntityException e) {
                    sendMessage("Помилка! Не знайдено чергу з таким ID!");
                }
            }
        }
    }
}

