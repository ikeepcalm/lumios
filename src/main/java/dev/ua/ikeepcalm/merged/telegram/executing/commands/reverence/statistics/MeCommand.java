/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.statistics;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class MeCommand
extends Executable {
    public void execute(Message origin) {
        User user = origin.getFrom();
        ReverenceChat linkedChatId = this.chatService.find(origin.getChatId());
        if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
            ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
            this.reply(origin, "\u21a3 \u0412\u0430\u0448\u0430 \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u21a2\n\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\n\u25c8 \u0420\u0435\u0439\u0442\u0438\u043d\u0433: " + foundUser.getReverence() + "\n\u25c8 \u041a\u0440\u0435\u0434\u0438\u0442\u0438: " + foundUser.getCredits() + "\n\u25c8 \u041e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f: " + foundUser.getSustainable() + "\n\u25c8 \u0413\u0430\u043c\u0430\u043d\u0435\u0446\u044c: " + foundUser.getBalance() + "\n\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\n\u21a3 \u0412\u0430\u0448\u0430 \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u21a2");
        } else {
            this.reply(origin, "\u0412\u0438 \u043d\u0435 \u0431\u0435\u0440\u0435\u0442\u0435 \u0443\u0447\u0430\u0441\u0442\u044c \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456 \u0431\u043e\u0442\u0443. \u0421\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 /register@queueupnow_bot!");
        }
    }
}

