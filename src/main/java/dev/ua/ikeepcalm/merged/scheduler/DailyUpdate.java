/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 */
package dev.ua.ikeepcalm.merged.scheduler;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyUpdate {

    private final UserService userService;

    public DailyUpdate(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron="0 0 22 * * *")
    public void executeDailyTask() {
        this.userService.updateAll();
    }
}

