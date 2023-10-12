/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.executing.callbacks;

import dev.ua.ikeepcalm.merged.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.merged.utils.DublicateUtil;
import dev.ua.ikeepcalm.merged.utils.QueueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class JoinCallback
extends Executable {

    @Autowired
    private QueueUtil queueUtil;

    private void updateMessage(Message origin, QueueItself queueItself) {
        AlterMessage generalMessage = new AlterMessage();
        generalMessage.setChatId(origin.getChatId());
        generalMessage.setMessageId((int)queueItself.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        generalMessage.setText(stringBuilder.toString());
        generalMessage.setReplyKeyboard(DublicateUtil.createMarkup(queueItself));
        queueItself.setMessageId(this.telegramService.sendAlterMessage(generalMessage).getMessageId().intValue());
        this.queueUtil.updateQueue(queueItself);
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = queueUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (!queueItself.getContents().contains(queueUser)) {
            queueItself.addUser(queueUser);
            this.updateMessage(origin.getMessage(), queueItself);
            this.telegramService.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", origin.getId());
        } else {
            this.telegramService.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", origin.getId());
        }
    }
}

