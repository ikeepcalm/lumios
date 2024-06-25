package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;

public class QueueUpdateUtil {

    public static EditMessage updateMessage(long chatId, SimpleQueue simpleQueue) {
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(simpleQueue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (SimpleUser iteSimpleUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteSimpleUser.getName()).append(" (@").append(iteSimpleUser.getUsername()).append(")\n");
            ++id;
        }
        editMessage.setText(stringBuilder.toString());
        editMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        return editMessage;
    }

    public static EditMessage updateMessage(long chatId, MixedQueue simpleQueue) {
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(simpleQueue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (MixedUser iteMixedUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteMixedUser.getName()).append(" (@").append(iteMixedUser.getUsername()).append(")\n");
            ++id;
        }
        editMessage.setText(stringBuilder.toString());
        editMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        return editMessage;
    }

}

