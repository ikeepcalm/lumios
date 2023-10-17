/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 */
package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class FlushCallback
extends Executable {
    private final QueueLifecycleUtil queueLifecycleUtil;

    public FlushCallback(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (queueItself.flushUser(queueUser)) {
            queueItself.setMessageId(this.absSender.sendEditMessage(QueueMarkupUtil.updateMessage(origin.getMessage(), queueItself)).getMessageId());
            this.queueLifecycleUtil.updateQueue(queueItself);
            this.absSender.sendAnswerCallbackQuery("Гарна робота! Тепер можещ трохи відпочити, і подивитися на те, як страждають інші...", origin.getId());
            if (queueItself.getContents().isEmpty()) {
                RemoveMessage removeMessage = new RemoveMessage((int)queueItself.getMessageId(), origin.getMessage().getChatId());
                this.absSender.sendRemoveMessage(removeMessage);
                this.queueLifecycleUtil.deleteQueue(queueItself);
            } else {
                this.absSender.sendTextMessage(QueueMarkupUtil.createNotification(origin.getMessage().getChatId(), queueItself));
            }
        } else {
            this.absSender.sendAnswerCallbackQuery("Ще не прийшла твоя черга! Або ти обманюєш мене, або хтось поламав чершу :>", origin.getId());
        }
        this.queueLifecycleUtil.updateQueue(queueItself);
    }
}

