package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ShotTask {

    private final ChatService chatService;
    private final ShotService shotService;

    public ShotTask(ChatService chatService, ShotService shotService) {
        this.chatService = chatService;
        this.shotService = shotService;
    }

    @Transactional
    @Scheduled(cron = "0 0 22 * * *")
    public void executeUpdateTask() {
        Iterable<ReverenceChat> chats = this.chatService.findAll();
        for (ReverenceChat chat : chats) {
//            try {
//                ChatShot chatShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now());
//                if (chatShot != null) {
//                    continue;
//                }
//            } catch (NoSuchElementException ignored) {}
            Set<ReverenceUser> users = chat.getUsers();
            ChatShot chatShot = new ChatShot();
            chatShot.setReverenceChat(chat);
            chatShot.setDate(LocalDate.now());
            List<UserShot> userShots = new ArrayList<>();
            for (ReverenceUser user : users) {
                UserShot userShot = new UserShot();
                userShot.setUserId(user.getUserId());
                userShot.setUsername(user.getUsername());
                userShot.setReverence(user.getReverence());
                userShots.add(userShot);
            }
            chatShot.setUserShots(userShots);
            this.shotService.save(chatShot);
        }
    }
}
