package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ShotTask {

    private final ChatService chatService;
    private final ShotService shotService;

    public ShotTask(ChatService chatService, ShotService shotService) {
        this.chatService = chatService;
        this.shotService = shotService;
    }

    @Async
    @Scheduled(cron = "0 45 22 * * *") // 22:45 - avoid collision with other tasks
    @Transactional
    public void executeShotTask() {
        try {
            log.info("Starting daily shot snapshot task");
            Iterable<LumiosChat> chats = this.chatService.findAll();
        for (LumiosChat chat : chats) {
            ChatShot chatShot;
            try {
                chatShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now());
                shotService.delete(chatShot);
            } catch (NoSuchEntityException e) {
                chatShot = new ChatShot();
            }

            chatShot.setChat(chat);
            chatShot.setDate(LocalDate.now());
            Set<LumiosUser> users = chat.getUsers();
            List<UserShot> userShots = new ArrayList<>();
            for (LumiosUser user : users) {
                UserShot userShot = new UserShot();
                userShot.setUserId(user.getUserId());
                userShot.setUsername(user.getUsername());
                userShot.setReverence(user.getReverence());
                userShots.add(userShot);
            }
            chatShot.setUserShots(userShots);
            this.shotService.save(chatShot);
        }
        log.info("Completed daily shot snapshot task");
        } catch (Exception e) {
            log.error("Failed to execute shot snapshot task", e);
        }
    }
}
