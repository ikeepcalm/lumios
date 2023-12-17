package dev.ua.ikeepcalm.queue.telegram.modules.queues.commands;

import dev.ua.ikeepcalm.queue.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queue.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.queue.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queue.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queue.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MixedCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        MixedQueue mixedQueue;
        if (!message.getText().equals("/mixed")) {
            String alias = message.getText()
                    .replace("/mixed@queueupnow_bot", "")
                    .replace("/mixed", "")
                    .toUpperCase();
            mixedQueue = mixedQueueLifecycle.createMixedQueue(alias);
        } else {
            mixedQueue = mixedQueueLifecycle.createMixedQueue();
        }
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        mixedQueue.addUser(queueUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(mixedQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : mixedQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(mixedQueue));
        Message sendTextMessage = this.absSender.sendTextMessage(queueMessage);
        this.absSender.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        mixedQueue.setMessageId(sendTextMessage.getMessageId());
        mixedQueueLifecycle.saveMixedQueue(mixedQueue);
    }
}

