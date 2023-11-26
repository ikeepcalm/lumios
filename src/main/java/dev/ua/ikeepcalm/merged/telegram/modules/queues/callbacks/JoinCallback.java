package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class JoinCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-join", "");
        String callbackQueryId = message.getId(); instantiateUpdate(message);
        QueueItself queueItself = queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (!queueItself.getContents().contains(queueUser)) {
            queueItself.addUser(queueUser);
            this.updateMessage(super.message, queueItself);
            this.absSender.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", callbackQueryId);
        } else {
            this.absSender.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", callbackQueryId);
        }
    }


    private void updateMessage(Message message, QueueItself queueItself) {
        EditMessage generalMessage = new EditMessage();
        generalMessage.setChatId(message.getChatId());
        generalMessage.setMessageId(queueItself.getMessageId());
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


}

