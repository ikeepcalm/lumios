package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimitsWipe {

    private final ChatService chatService;

    public LimitsWipe(ChatService chatService) {
        this.chatService = chatService;
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void executeUpdateTask() {
        Iterable<LumiosChat> chats = chatService.findAll();
        for (LumiosChat chat : chats) {
            chat.setSummaryLimit(2);
            chat.setCommunicationLimit(10);
            chatService.save(chat);
        }
    }

}
