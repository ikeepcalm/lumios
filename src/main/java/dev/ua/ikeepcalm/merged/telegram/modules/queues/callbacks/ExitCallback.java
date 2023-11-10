package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class ExitCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-exit", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (queueItself.getContents().peek() != null && queueItself.getContents().peek().equals(queueUser)) {
            if (queueItself.flushUser(queueUser)) {
                queueItself.setMessageId(this.absSender.sendEditMessage(QueueMarkupUtil.updateMessage(super.message, queueItself)).getMessageId());
                this.queueLifecycleUtil.updateQueue(queueItself);
                this.absSender.sendAnswerCallbackQuery("Ха! Ти помилився кнопкою, але не переймайся, я сповіщу наступного за тобою про його чергу", callbackQueryId);
                if (queueItself.getContents().isEmpty()) {
                    RemoveMessage removeMessage = new RemoveMessage(queueItself.getMessageId(), super.message.getChatId());
                    this.absSender.sendRemoveMessage(removeMessage);
                } else {
                    this.absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), queueItself));
                }
            }
        } else {
            if (queueItself.getContents().contains(queueUser)) {
                queueItself.removeUser(queueUser);
                queueItself.setMessageId(absSender.sendEditMessage(QueueMarkupUtil.updateMessage(super.message, queueItself)).getMessageId());
                queueLifecycleUtil.updateQueue(queueItself);
                absSender.sendAnswerCallbackQuery("Хочеш вийти? Ну добре, виходь, ніхто ж тебе тут насильно не тримає...", callbackQueryId);
            }
        }
    }
}

