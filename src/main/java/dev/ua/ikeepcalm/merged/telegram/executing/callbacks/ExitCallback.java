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
public class ExitCallback
extends Executable {
    @Autowired
    private QueueUtil queueUtil;

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (queueItself.getContents().peek().equals(queueUser)) {
            if (queueItself.flushUser(queueUser)) {
                queueItself.setMessageId(this.telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queueItself)).getMessageId().intValue());
                this.queueUtil.updateQueue(queueItself);
                this.telegramService.sendAnswerCallbackQuery("\u0425\u0430! \u0422\u0438 \u043f\u043e\u043c\u0438\u043b\u0438\u0432\u0441\u044f \u043a\u043d\u043e\u043f\u043a\u043e\u044e. \u0410\u043b\u0435 \u043d\u0456\u0447\u043e\u0433\u043e \u0441\u0442\u0440\u0430\u0448\u043d\u043e\u0433\u043e, \u044f \u0441\u043f\u043e\u0432\u0456\u0449\u0443 \u043d\u0430\u0441\u0442\u0443\u043f\u043d\u043e\u0433\u043e \u0437\u0430 \u0442\u043e\u0431\u043e\u044e!", origin.getId());
                if (queueItself.getContents().isEmpty()) {
                    PurgeMessage purgeMessage = new PurgeMessage((int)queueItself.getMessageId(), origin.getMessage().getChatId());
                    this.telegramService.sendPurgeMessage(purgeMessage);
                } else {
                    this.telegramService.sendMultiMessage(DublicateUtil.createNotification(origin.getMessage().getChatId(), queueItself));
                }
            }
        } else if (queueItself.getContents().contains(queueUser)) {
            queueItself.removeUser(queueUser);
            queueItself.setMessageId(this.telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queueItself)).getMessageId().intValue());
            this.queueUtil.updateQueue(queueItself);
            this.telegramService.sendAnswerCallbackQuery("\u0429\u043e\u0441\u044c \u0432\u0430\u0436\u043b\u0438\u0432\u0456\u0448\u0435 \u0437\u0430 \u0446\u0435? \u0425\u043c\u043c... \u0422\u0432\u043e\u0454 \u043f\u0440\u0430\u0432\u043e", origin.getId());
            if (queueItself.getContents().isEmpty()) {
                PurgeMessage purgeMessage = new PurgeMessage((int)queueItself.getMessageId(), origin.getMessage().getChatId());
                this.telegramService.sendPurgeMessage(purgeMessage);
                this.queueUtil.deleteQueue(queueItself);
            }
        } else {
            this.telegramService.sendAnswerCallbackQuery("\u0429\u043e\u0431 \u0432\u0438\u0439\u0442\u0438 \u0437 \u0446\u0456\u0454\u0457 \u0447\u0435\u0440\u0433\u0438 \u0442\u0440\u0435\u0431\u0430 \u0441\u043f\u043e\u0447\u0430\u0442\u043a\u0443 \u043f\u0440\u0438\u0454\u0434\u043d\u0430\u0442\u0438\u0441\u044f \u0434\u043e \u043d\u0435\u0457! \u0413\u0430\u0434\u0430\u044e, \u0449\u043e \u0442\u0438 \u0442\u0438\u0446\u043d\u0443\u0432 \u0441\u044e\u0434\u0438 \u0432\u0438\u043f\u0430\u0434\u043a\u043e\u0432\u043e ^-^", origin.getId());
        }
    }
}

