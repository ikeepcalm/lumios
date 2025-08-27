package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.wrappers.DifferenceWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class CleanupTask {

    private final ShotService shotService;
    private final UserService userService;
    private final ChatService chatService;

    public CleanupTask(ShotService shotService, UserService userService, ChatService chatService) {
        this.shotService = shotService;
        this.userService = userService;
        this.chatService = chatService;
    }

    @Async
    @Scheduled(cron = "0 0 12 */7 * ?")
    public void executeUsersCleanupTask() {
        try {
            log.info("Starting weekly users cleanup task");
        Iterable<LumiosChat> chats = chatService.findAll();
        for (LumiosChat chat : chats) {
            try {
                ChatShot startShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now().minusDays(1));
                ChatShot endShot = shotService.findByChatIdAndDate(chat.getChatId(), LocalDate.now().minusDays(14));
                for (LumiosUser user : chat.getUsers()) {
                    UserShot startUserShot = startShot.getUserShots().stream().filter(userShot -> userShot.getUserId().equals(user.getUserId())).findFirst().orElseThrow(NoSuchEntityException::new);
                    UserShot endUserShot = endShot.getUserShots().stream().filter(userShot -> userShot.getUserId().equals(user.getUserId())).findFirst().orElseThrow(NoSuchEntityException::new);
                    DifferenceWrapper differenceWrapper = new DifferenceWrapper(startUserShot, endUserShot);
                    if (differenceWrapper.getReverence() == 0) { // No activity, delete user
                        userService.delete(user);
                    }
                }
            } catch (NoSuchEntityException e) {
                log.debug("No shots found for cleanup in chat: {}", chat.getChatId());
            }
        }
        log.info("Completed weekly users cleanup task");
        } catch (Exception e) {
            log.error("Failed to execute users cleanup task", e);
        }
    }

    @Async
    @Scheduled(cron = "0 0 12 */7 * ?")
    public void executeChatsCleanupTask() {
        try {
            log.info("Starting weekly chats cleanup task");
        Iterable<LumiosChat> chats = chatService.findAll();
        for (LumiosChat chat : chats) {
            if (chat.getName() == null) {
                chatService.delete(chat);
            }
        }
        log.info("Completed weekly chats cleanup task");
        } catch (Exception e) {
            log.error("Failed to execute chats cleanup task", e);
        }
    }
}
