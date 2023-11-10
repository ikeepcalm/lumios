package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class NotifyCallback extends CallbackParent {
    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-notify", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        QueueItself queueItself = queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), queueItself));
        absSender.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
    }
}

