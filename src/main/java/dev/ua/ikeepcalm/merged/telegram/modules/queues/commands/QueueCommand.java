package dev.ua.ikeepcalm.merged.telegram.modules.queues.commands;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class QueueCommand extends CommandParent {
    private final QueueLifecycleUtil queueLifecycleUtil;

    public QueueCommand(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    public void execute(Message origin) {
        QueueItself queueItself;
        if (!origin.getText().equals("/queue")) {
            String alias = origin.getText().replace("/queue ", "").toUpperCase();
            queueItself = this.queueLifecycleUtil.createQueue(alias);
        } else {
            queueItself = this.queueLifecycleUtil.createQueue();
        }
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        queueItself.addUser(queueUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(origin.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queueItself));
        Message message = this.absSender.sendTextMessage(queueMessage);
        this.absSender.pinChatMessage(origin.getChatId(), message.getMessageId());
        queueItself.setMessageId(message.getMessageId());
    }
}

