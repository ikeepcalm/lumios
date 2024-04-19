package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CallbackParent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class NotifyCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-notify", "");
        String callbackQueryId = message.getId();
        SimpleQueue simpleQueue;
        try {
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            telegramClient.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
            telegramClient.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }
}

