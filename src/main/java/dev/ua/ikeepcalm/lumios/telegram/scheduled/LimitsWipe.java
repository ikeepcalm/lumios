package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class LimitsWipe {

    private final ChatService chatService;

    public LimitsWipe(ChatService chatService) {
        this.chatService = chatService;
    }

    @Async
    @Transactional
    @Scheduled(cron = "0 30 22 * * *")
    public void executeUpdateTask() {
        try {
            log.info("Starting daily limits reset");
            
            int updatedChats = chatService.batchUpdateLimits(2, 10);
            
            log.info("Successfully reset limits for {} chats", updatedChats);
        } catch (Exception e) {
            log.error("Failed to reset daily limits", e);
            throw e;
        }
    }
}