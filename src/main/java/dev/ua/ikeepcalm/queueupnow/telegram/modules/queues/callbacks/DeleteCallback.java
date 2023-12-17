package dev.ua.ikeepcalm.queue.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.queue.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queue.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.queue.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.queue.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.UUID;

@Component
public class DeleteCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-delete", "");
        instantiateUpdate(message);
        for (ChatMember chatMember : absSender.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
            if (chatMember.getUser().getId().equals(message.getFrom().getId())) {
                SimpleQueue simpleQueue = simpleQueueLifecycle.getSimpleQueue(UUID.fromString(receivedCallback));
                simpleQueueLifecycle.deleteSimpleQueue(simpleQueue);
                absSender.sendRemoveMessage(new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId()));
                TextMessage textMessage = new TextMessage();
                textMessage.setChatId(message.getMessage().getChatId());
                textMessage.setText("@".concat(message.getFrom().getUserName()).concat(" видалив чергу: ").concat(simpleQueue.getAlias()).concat("!"));
                sendMessage(textMessage);
                break;
            }
        }
    }
}

