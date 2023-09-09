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
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

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
        generalMessage.setReplyKeyboard((ReplyKeyboard)DublicateUtil.createMarkup(queueItself));
        queueItself.setMessageId(this.telegramService.sendAlterMessage(generalMessage).getMessageId().intValue());
        this.queueUtil.updateQueue(queueItself);
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (!queueItself.getContents().contains(queueUser)) {
            queueItself.addUser(queueUser);
            this.updateMessage(origin.getMessage(), queueItself);
            this.telegramService.sendAnswerCallbackQuery("\u0423\u0441\u043f\u0456\u0448\u043d\u043e \u0437\u0430\u0431\u0440\u043e\u043d\u044c\u043e\u0432\u0430\u043d\u043e \u043c\u0456\u0441\u0446\u0435 \u0443 \u0447\u0435\u0440\u0437\u0456!", origin.getId());
        } else {
            this.telegramService.sendAnswerCallbackQuery("\u0412\u0438 \u0432\u0436\u0435 \u0437\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0435\u0441\u044c \u0443 \u0446\u0456\u0439 \u0447\u0435\u0440\u0437\u0456!", origin.getId());
        }
    }
}

