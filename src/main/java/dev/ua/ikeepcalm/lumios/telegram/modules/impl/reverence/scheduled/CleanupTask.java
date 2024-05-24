package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.wrappers.DifferenceWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CleanupTask {

    private static final Logger log = LoggerFactory.getLogger(CleanupTask.class);
    private final ShotService shotService;
    private final UserService userService;
    private final ChatService chatService;

    public CleanupTask(ShotService shotService, UserService userService, ChatService chatService) {
        this.shotService = shotService;
        this.userService = userService;
        this.chatService = chatService;
    }

    @Scheduled(cron = "0 22 */2 * * *")
    public void executeUsersCleanupTask() {
        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats) {
            try {
                ChatShot startShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now().minusDays(1));
                ChatShot endShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now().minusDays(14));
                for (ReverenceUser user : chat.getUsers()) {
                    UserShot startUserShot = startShot.getUserShots().stream().filter(userShot -> userShot.getUserId().equals(user.getUserId())).findFirst().orElseThrow(NoSuchEntityException::new);
                    UserShot endUserShot = endShot.getUserShots().stream().filter(userShot -> userShot.getUserId().equals(user.getUserId())).findFirst().orElseThrow(NoSuchEntityException::new);
                    DifferenceWrapper differenceWrapper = new DifferenceWrapper(startUserShot, endUserShot);
                    if (differenceWrapper.getReverence() > 0) {
                        userService.delete(user);
                    }
                }
            } catch (NoSuchEntityException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Scheduled(cron = "0 22 */2 * * *")
    public void executeChatsCleanupTask() {
        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats) {
            if (chat.getName() == null) {
                chatService.delete(chat);
            }
        }
    }

}
