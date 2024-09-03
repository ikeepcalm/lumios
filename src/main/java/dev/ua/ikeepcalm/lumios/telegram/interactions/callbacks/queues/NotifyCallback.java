package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.QueueMarkupUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalTime;
import java.util.UUID;

@Component
@BotCallback(endsWith = "simple-notify")
public class NotifyCallback extends ServicesShortcut implements Interaction {

    private LocalTime lastNotifyTime;

    @Override

    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-simple-notify", "");
        String callbackQueryId = message.getId();
        SimpleQueue simpleQueue;
        try {
            if (lastNotifyTime != null && lastNotifyTime.plusMinutes(1).isAfter(LocalTime.now())) {
                telegramClient.sendAnswerCallbackQuery("Ви можете надсилати сповіщення лише раз на хвилину!", callbackQueryId);
                return;
            }
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            telegramClient.sendTextMessage(QueueMarkupUtil.createNotification(message.getMessage().getChatId(), simpleQueue));
            telegramClient.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", callbackQueryId);
            lastNotifyTime = LocalTime.now();
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }
}

