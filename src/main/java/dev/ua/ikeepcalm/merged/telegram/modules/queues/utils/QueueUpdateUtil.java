package dev.ua.ikeepcalm.merged.telegram.modules.queues.utils;

import dev.ua.ikeepcalm.merged.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class QueueUpdateUtil {


    public static EditMessage updateMessage(Message message, SimpleQueue simpleQueue) {
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(simpleQueue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        editMessage.setText(stringBuilder.toString());
        editMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        return editMessage;
    }

    public static EditMessage updateMessage(Message message, MixedQueue simpleQueue) {
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(simpleQueue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        editMessage.setText(stringBuilder.toString());
        editMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        return editMessage;
    }

}

