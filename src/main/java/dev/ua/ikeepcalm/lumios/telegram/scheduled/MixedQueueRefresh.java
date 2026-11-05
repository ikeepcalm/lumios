package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class MixedQueueRefresh {

    private final QueueService queueService;
    private final TelegramClient telegramClient;

    public MixedQueueRefresh(QueueService queueService, TelegramClient telegramClient) {
        this.queueService = queueService;
        this.telegramClient = telegramClient;
    }

    @Transactional(readOnly = true)
    @EventListener(ApplicationReadyEvent.class)
    public void refreshMixedQueueMessages() {
        int refreshed = 0;
        for (MixedQueue queue : queueService.findAllMixed()) {
            try {
                telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(queue.getChatId(), queue));
                refreshed++;
            } catch (RuntimeException e) {
                // "message is not modified" after the first run, or the message was deleted - neither matters here
                log.debug("Skipped refreshing mixed queue {}: {}", queue.getId(), e.getMessage());
            }
        }
        log.info("Refreshed {} mixed queue messages", refreshed);
    }
}
