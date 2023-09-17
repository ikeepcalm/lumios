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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

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
                this.telegramService.sendAnswerCallbackQuery("Ха! Ти помилився кнопкою, але не переймайся, я сповіщу наступного за тобою про його чергу", origin.getId());
                if (queueItself.getContents().isEmpty()) {
                    PurgeMessage purgeMessage = new PurgeMessage((int) queueItself.getMessageId(), origin.getMessage().getChatId());
                    this.telegramService.sendPurgeMessage(purgeMessage);
                } else {
                    this.telegramService.sendMultiMessage(DublicateUtil.createNotification(origin.getMessage().getChatId(), queueItself));
                }
            }
        } else if (queueItself.getContents().contains(queueUser)) {
            queueItself.removeUser(queueUser);
            queueItself.setMessageId(telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queueItself)).getMessageId());
            this.queueUtil.updateQueue(queueItself);
            this.telegramService.sendAnswerCallbackQuery("Щось важливіше, ніж це? Ну нехай, не моя справа :)", origin.getId());
            if (queueItself.getContents().isEmpty()) {
                PurgeMessage purgeMessage = new PurgeMessage((int) queueItself.getMessageId(), origin.getMessage().getChatId());
                this.telegramService.sendPurgeMessage(purgeMessage);
                this.queueUtil.deleteQueue(queueItself);
            }
        } else {
            this.telegramService.sendAnswerCallbackQuery("Щоб вийти з черги, потрібно спочатку бути в ній. Промазав по кнопці?", origin.getId());
        }
    }
}

