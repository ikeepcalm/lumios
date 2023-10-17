/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.modules.queues.commands;

import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class SaveQueuesCommand
extends Executable {
    private final QueueLifecycleUtil queueLifecycleUtil;

    public SaveQueuesCommand(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    public void execute(Message origin) {
        User user = origin.getFrom();
        if (user.getId() == 586319182L) {
            this.reply(origin, "Усі черги були збережені!");
            this.queueLifecycleUtil.saveHashMapToFile();
        }
    }
}

