/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside;

import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class ForceUpdateCommand
extends Executable {
    public void execute(Message origin) {
        User user = origin.getFrom();
        if (user.getId() == 586319182L) {
            this.reply(origin, "\u041f\u0440\u0438\u043c\u0443\u0441\u043e\u0432\u0435 \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f \u043a\u0440\u0435\u0434\u0438\u0442\u0456\u0432 \u0456 \u0431\u0430\u043b\u0430\u043d\u0441\u0443 \u0432\u0438\u043a\u043e\u043d\u0430\u043d\u043e!");
            this.userService.updateAll();
        } else {
            this.telegramService.sendVideo(origin.getChatId(), "src/main/resources/crazy.mp4", origin.getMessageId());
        }
    }
}

