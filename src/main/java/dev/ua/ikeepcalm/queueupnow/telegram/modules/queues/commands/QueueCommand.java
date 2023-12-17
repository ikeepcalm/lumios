package dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class QueueCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        SimpleQueue simpleQueue;
        if (!message.getText().equals("/queue")) {
            String alias = message.getText()
                    .replace("/queue@queueupnow_bot", "")
                    .replace("/queue", "")
                    .toUpperCase();
            simpleQueue = simpleQueueLifecycle.createSimpleQueue(alias);
        } else {
            simpleQueue = simpleQueueLifecycle.createSimpleQueue();
        }
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        simpleQueue.addUser(queueUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : simpleQueue.getContents()) {
            stringBuilder.append("ID: ")
                    .append(id).append(" - ")
                    .append(iteQueueUser.getName())
                    .append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
        Message sendTextMessage = this.absSender.sendTextMessage(queueMessage);
        this.absSender.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        simpleQueue.setMessageId(sendTextMessage.getMessageId());
        simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
    }
}

