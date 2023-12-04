package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueUpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class NotifyCallback extends CallbackParent {
    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-notify", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        SimpleQueue simpleQueue = simpleQueueLifecycle.getSimpleQueue(UUID.fromString(receivedCallback));
        absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
        absSender.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
    }
}

