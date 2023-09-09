/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 */
package dev.ua.ikeepcalm.merged.telegram.executing.callbacks;

import dev.ua.ikeepcalm.merged.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.PurgeMessage;
import dev.ua.ikeepcalm.merged.utils.DublicateUtil;
import dev.ua.ikeepcalm.merged.utils.QueueUtil;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class FlushCallback
extends Executable {
    @Autowired
    private QueueUtil queueUtil;

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (queueItself.flushUser(queueUser)) {
            queueItself.setMessageId(this.telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queueItself)).getMessageId().intValue());
            this.queueUtil.updateQueue(queueItself);
            this.telegramService.sendAnswerCallbackQuery("\u0413\u0430\u0440\u043d\u0430 \u0440\u043e\u0431\u043e\u0442\u0430! \u0422\u0435\u043f\u0435\u0440 \u043c\u043e\u0436\u0435\u0449 \u0442\u0440\u043e\u0445\u0438 \u0432\u0456\u0434\u043f\u043e\u0447\u0438\u0442\u0438, \u0456 \u043f\u043e\u0434\u0438\u0432\u0438\u0442\u0438\u0441\u044f \u043d\u0430 \u0442\u0435, \u044f\u043a \u0441\u0442\u0440\u0430\u0436\u0434\u0430\u044e\u0442\u044c \u0456\u043d\u0448\u0456...", origin.getId());
            if (queueItself.getContents().isEmpty()) {
                PurgeMessage purgeMessage = new PurgeMessage((int)queueItself.getMessageId(), origin.getMessage().getChatId());
                this.telegramService.sendPurgeMessage(purgeMessage);
                this.queueUtil.deleteQueue(queueItself);
            } else {
                this.telegramService.sendMultiMessage(DublicateUtil.createNotification(origin.getMessage().getChatId(), queueItself));
            }
        } else {
            this.telegramService.sendAnswerCallbackQuery("\u0429\u0435 \u043d\u0435 \u043f\u0440\u0438\u0439\u0448\u043b\u0430 \u0442\u0432\u043e\u044f \u0447\u0435\u0440\u0433\u0430! \u0410\u0431\u043e \u0442\u0438 \u043e\u0431\u043c\u0430\u043d\u044e\u0454\u0448 \u043c\u0435\u043d\u0435, \u0430\u0431\u043e \u0445\u0442\u043e\u0441\u044c \u043f\u043e\u043b\u0430\u043c\u0430\u0432 \u0447\u0435\u0440\u0448\u0443 :>", origin.getId());
        }
        this.queueUtil.updateQueue(queueItself);
    }
}

