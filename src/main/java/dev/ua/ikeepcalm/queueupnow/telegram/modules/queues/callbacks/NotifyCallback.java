package dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.utils.QueueMarkupUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class NotifyCallback extends CallbackParent {
    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-notify", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        SimpleQueue simpleQueue;
        try {
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            absSender.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
            absSender.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
        } catch (NoSuchEntityException e) {
            absSender.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }
}

