package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalTime;
import java.util.UUID;

@Component
public class NotifyCallback extends CallbackParent {

    private LocalTime lastNotifyTime;

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-notify", "");
        String callbackQueryId = message.getId();
        SimpleQueue simpleQueue;
        try {
            if (lastNotifyTime != null && lastNotifyTime.plusMinutes(1).isAfter(LocalTime.now())) {
                telegramClient.sendAnswerCallbackQuery("Ви можете надсилати сповіщення лише раз на хвилину!", callbackQueryId);
                return;
            }
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            telegramClient.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
            telegramClient.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
            lastNotifyTime = LocalTime.now();
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }
}

