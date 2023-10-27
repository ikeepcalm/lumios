package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class DeleteCallback extends CommandParent {
    private final QueueLifecycleUtil queueLifecycleUtil;

    public DeleteCallback(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        if (origin.getFrom().getUserName().equals("ikeepcalm")){
            queueLifecycleUtil.deleteQueue(queueItself);
            absSender.sendRemoveMessage(new RemoveMessage(queueItself.getMessageId(), origin.getMessage().getChatId()));
        } else {
            absSender.sendAnswerCallbackQuery("Авторизувати цю дію може лише власник боту!", origin.getId());
        }
    }
}

