package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class JoinCallback
extends Executable {

    private final QueueLifecycleUtil queueLifecycleUtil;

    public JoinCallback(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    private void updateMessage(Message origin, QueueItself queueItself) {
        EditMessage generalMessage = new EditMessage();
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
        generalMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queueItself));
        queueItself.setMessageId(this.absSender.sendEditMessage(generalMessage).getMessageId());
        this.queueLifecycleUtil.updateQueue(queueItself);
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(origin.getFrom().getFirstName());
        queueUser.setAccountId(origin.getFrom().getId());
        queueUser.setUsername(origin.getFrom().getUserName());
        if (!queueItself.getContents().contains(queueUser)) {
            queueItself.addUser(queueUser);
            this.updateMessage(origin.getMessage(), queueItself);
            this.absSender.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", origin.getId());
        } else {
            this.absSender.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", origin.getId());
        }
    }
}

