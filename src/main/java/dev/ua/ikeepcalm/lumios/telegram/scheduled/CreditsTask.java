package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreditsTask {

    private final UserService userService;

    public CreditsTask(UserService userService) {
        this.userService = userService;
    }

    @Async
    @Scheduled(cron = "0 0 22 * * *") // 22:00 daily
    public void executeUpdateTask() {
        try {
            log.info("Starting daily user update task");
            this.userService.updateAll();
            log.info("Completed daily user update task");
        } catch (Exception e) {
            log.error("Failed to execute user update task", e);
        }
    }

    @Async
    @Scheduled(cron = "0 15 22 */2 * *") // 22:15 every 2 days - avoid collision
    public void executeIncreaseTask() {
        try {
            log.info("Starting user increase task");
            this.userService.increaseAll();
            log.info("Completed user increase task");
        } catch (Exception e) {
            log.error("Failed to execute user increase task", e);
        }
    }
}
