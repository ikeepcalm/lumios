/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 */
package dev.ua.ikeepcalm.merged.scheduler;

import dev.ua.ikeepcalm.merged.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.telegram.servicing.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyUpdate {
    @Autowired
    private UserService userService;
    @Autowired
    private TelegramService telegramService;

    @Scheduled(cron="0 0 21 * * *")
    public void executeDailyTask() {
        this.userService.updateAll();
    }
}

