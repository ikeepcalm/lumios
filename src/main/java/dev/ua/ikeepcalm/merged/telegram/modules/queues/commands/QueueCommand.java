package dev.ua.ikeepcalm.merged.telegram.modules.queues.commands;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class QueueCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        QueueItself queueItself;
        if (!message.getText().equals("/queue")) {
            String alias = message.getText().replace("/queue ", "").toUpperCase();
            queueItself = queueLifecycleUtil.createQueue(alias);
        } else {
            queueItself = queueLifecycleUtil.createQueue();
        }
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        queueItself.addUser(queueUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queueItself));
        Message sendTextMessage = this.absSender.sendTextMessage(queueMessage);
        this.absSender.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        queueItself.setMessageId(sendTextMessage.getMessageId());
    }
}

