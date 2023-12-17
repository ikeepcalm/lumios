package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.schedules;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyUpdate {

    private final UserService userService;

    public DailyUpdate(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void executeDailyTask() {
        this.userService.updateAll();
    }
}

