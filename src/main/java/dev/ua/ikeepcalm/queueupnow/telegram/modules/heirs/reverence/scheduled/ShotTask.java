package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.scheduled;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ReverenceChatShot;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ReverenceUserShot;
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

    @Scheduled(cron = "0 0 22 * * *")
    public void executeUpdateTask() {
        Iterable<ReverenceChat> chats = this.chatService.findAll();
        for (ReverenceChat chat : chats) {
            Set<ReverenceUser> users = chat.getUsers();
            ReverenceChatShot chatShot = new ReverenceChatShot();
            chatShot.setReverenceChat(chat);
            chatShot.setDate(LocalDate.now());
            List<ReverenceUserShot> userShots = new ArrayList<>();
            for (ReverenceUser user : users) {
                ReverenceUserShot userShot = new ReverenceUserShot();
                userShot.setUserId(user.getUserId());
                userShot.setUsername(user.getUsername());
                userShot.setReverence(user.getReverence());
                userShots.add(userShot);
            }
            chatShot.setUsers(userShots);
            this.shotService.save(chatShot);
        }
    }
}
