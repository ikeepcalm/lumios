package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotChannel;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@BotChannel
public class NewsUpdate extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(NewsUpdate.class);

    @Value("${telegram.news.interval.seconds}")
    private long intervalSeconds;

    @Value("${telegram.news.channel.id}")
    private long channelId;

    @Override
    public void fireInteraction(Update update) {
        if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            if (update.getChannelPost().getChatId() == channelId) {
                try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
                    List<LumiosChat> chats = (List<LumiosChat>) chatService.findAll();
                    for (int i = 0; i < chats.size(); i++) {
                        LumiosChat chat = chats.get(i);
                        long delay = i * intervalSeconds;
                        scheduler.schedule(() -> sendMessageToChat(chat.getChatId(), update.getChannelPost().getChatId(), update.getChannelPost().getMessageId()), delay, TimeUnit.SECONDS);
                    }
                }
            }
        }
    }

    private void sendMessageToChat(long chatId, long fromChatId, int messageId) {
        try {
            telegramClient.sendForwardMessage(chatId, fromChatId, messageId);
        } catch (TelegramApiException e) {
            log.error("Error while sending message to chat", e);
        }
    }
}
