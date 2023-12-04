package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class ExitCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-exit", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        SimpleQueue simpleQueue =simpleQueueLifecycle.getSimpleQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (simpleQueue.getContents().peek() != null && simpleQueue.getContents().peek().equals(queueUser)) {
            if (simpleQueue.flushUser(queueUser)) {
                simpleQueue.setMessageId(this.absSender.sendEditMessage(QueueUpdateUtil.updateMessage(super.message, simpleQueue)).getMessageId());
                simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
                this.absSender.sendAnswerCallbackQuery("Ха! Ти помилився кнопкою, але не переймайся, я сповіщу наступного за тобою про його чергу", callbackQueryId);
                if (simpleQueue.getContents().isEmpty()) {
                    RemoveMessage removeMessage = new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId());
                    this.absSender.sendRemoveMessage(removeMessage);
                } else {
                    this.absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
                }
            }
        } else {
            if (simpleQueue.getContents().contains(queueUser)) {
                simpleQueue.removeUser(queueUser);
                simpleQueue.setMessageId(absSender.sendEditMessage(QueueUpdateUtil.updateMessage(super.message, simpleQueue)).getMessageId());
                simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
                absSender.sendAnswerCallbackQuery("Хочеш вийти? Ну добре, виходь, ніхто ж тебе тут насильно не тримає...", callbackQueryId);
            }
        }
    }
}

