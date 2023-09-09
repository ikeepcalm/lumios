/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside;

import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import dev.ua.ikeepcalm.merged.utils.QueueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class ForceSaveQueues
extends Executable {
    @Autowired
    private QueueUtil queueUtil;

    public void execute(Message origin) {
        User user = origin.getFrom();
        if (user.getId() == 586319182L) {
            this.reply(origin, "\u0423\u0441\u0456 \u0447\u0435\u0440\u0433\u0438 \u0431\u0443\u043b\u0438 \u0437\u0431\u0435\u0440\u0435\u0436\u0435\u043d\u0456!");
            this.queueUtil.saveHashMapToFile();
        } else {
            this.telegramService.sendVideo(origin.getChatId(), "src/main/resources/crazy.mp4", origin.getMessageId());
        }
    }
}

