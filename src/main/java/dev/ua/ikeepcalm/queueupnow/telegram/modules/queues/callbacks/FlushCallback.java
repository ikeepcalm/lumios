package dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.utils.QueueUpdateUtil;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class FlushCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-flush", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        SimpleQueue simpleQueue = simpleQueueLifecycle.getSimpleQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (simpleQueue.flushUser(queueUser)) {
            simpleQueue.setMessageId(this.absSender.sendEditMessage(QueueUpdateUtil.updateMessage(super.message, simpleQueue)).getMessageId());
            simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
            this.absSender.sendAnswerCallbackQuery("Гарна робота! Тепер можеш трохи відпочити, і подивитися на те, як страждають інші...", callbackQueryId);
            if (simpleQueue.getContents().isEmpty()) {
                RemoveMessage removeMessage = new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId());
                this.absSender.sendRemoveMessage(removeMessage);
                simpleQueueLifecycle.deleteSimpleQueue(simpleQueue);
            } else {
                this.absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
            }
        } else {
            this.absSender.sendAnswerCallbackQuery("Ще не прийшла твоя черга! Або ти обманюєш мене, або хтось поламав чергу :>", callbackQueryId);
        }
        simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
    }
}

