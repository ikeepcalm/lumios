package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class FlushCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-flush", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (queueItself.flushUser(queueUser)) {
            queueItself.setMessageId(this.absSender.sendEditMessage(QueueMarkupUtil.updateMessage(super.message, queueItself)).getMessageId());
            this.queueLifecycleUtil.updateQueue(queueItself);
            this.absSender.sendAnswerCallbackQuery("Гарна робота! Тепер можеш трохи відпочити, і подивитися на те, як страждають інші...", callbackQueryId);
            if (queueItself.getContents().isEmpty()) {
                RemoveMessage removeMessage = new RemoveMessage(queueItself.getMessageId(), super.message.getChatId());
                this.absSender.sendRemoveMessage(removeMessage);
                this.queueLifecycleUtil.deleteQueue(queueItself);
            } else {
                this.absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), queueItself));
            }
        } else {
            this.absSender.sendAnswerCallbackQuery("Ще не прийшла твоя черга! Або ти обманюєш мене, або хтось поламав чергу :>", callbackQueryId);
        }
        this.queueLifecycleUtil.updateQueue(queueItself);
    }
}

