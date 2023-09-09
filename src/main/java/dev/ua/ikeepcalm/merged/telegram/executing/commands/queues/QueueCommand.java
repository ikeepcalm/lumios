/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.queues;

import dev.ua.ikeepcalm.merged.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.merged.utils.DublicateUtil;
import dev.ua.ikeepcalm.merged.utils.QueueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Component
public class QueueCommand
extends Executable {
    @Autowired
    private QueueUtil queueUtil;

    public void execute(Message origin) {
        QueueItself queueItself;
        if (!origin.getText().equals("/queue")) {
            String alias = origin.getText().replace("/queue ", "").toUpperCase();
            queueItself = this.queueUtil.createQueue(origin.getChatId(), alias);
        } else {
            queueItself = this.queueUtil.createQueue(origin.getChatId());
        }
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        queueItself.addUser(queueUser);
        MultiMessage queueMessage = new MultiMessage();
        queueMessage.setChatId(origin.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard((ReplyKeyboard)DublicateUtil.createMarkup(queueItself));
        Message message = this.telegramService.sendMultiMessage(queueMessage);
        this.telegramService.pinChatMessage(origin.getChatId(), message.getMessageId().intValue());
        queueItself.setMessageId(message.getMessageId().intValue());
    }
}

