package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class DeleteCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-delete", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        if (message.getFrom().getUserName().equals("ikeepcalm")){
            queueLifecycleUtil.deleteQueue(queueItself);
            absSender.sendRemoveMessage(new RemoveMessage(queueItself.getMessageId(), super.message.getChatId()));
        } else {
            absSender.sendAnswerCallbackQuery("Авторизувати цю дію може лише власник боту!", callbackQueryId);
        }
    }
}

