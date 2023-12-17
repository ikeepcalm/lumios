package dev.ua.ikeepcalm.queue.telegram.modules.queues.callbacks.mixed;

import dev.ua.ikeepcalm.queue.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queue.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.queue.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.queue.telegram.modules.queues.utils.QueueUpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component("mixed-join")
public class JoinCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-join", "");
        String callbackQueryId = message.getId(); instantiateUpdate(message);
        MixedQueue mixedQueue = mixedQueueLifecycle.getMixedQueue(UUID.fromString(receivedCallback));
        QueueUser queueUser = new QueueUser();
        queueUser.setName(message.getFrom().getFirstName());
        queueUser.setAccountId(message.getFrom().getId());
        queueUser.setUsername(message.getFrom().getUserName());
        if (!mixedQueue.getContents().contains(queueUser)) {
            mixedQueue.addUser(queueUser);
            mixedQueue.setMessageId(absSender.sendEditMessage
                    (QueueUpdateUtil.updateMessage(message.getMessage(), mixedQueue))
                    .getMessageId());
            mixedQueueLifecycle.saveMixedQueue(mixedQueue);
            this.absSender.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", callbackQueryId);
        } else {
            this.absSender.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", callbackQueryId);
        }
    }

}

