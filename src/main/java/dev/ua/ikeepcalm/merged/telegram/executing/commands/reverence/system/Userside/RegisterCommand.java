/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class RegisterCommand
extends Executable {
    public void execute(Message origin) {
        User user = origin.getFrom();
        if (this.userService.checkIfUserExists(user.getId(), this.chatService.find(origin.getChatId()))) {
            this.reply(origin, "\u0412\u0430\u0448 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u0432\u0436\u0435 \u0437\u0430\u0440e\u0454\u0441\u0442\u0440\u043e\u0432\u0430\u043d\u043e \u0443 \u0446\u044c\u043e\u043c\u0443 \u0447\u0430\u0442\u0456!\n\u041d\u0435\u043c\u0430\u0454 \u043d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e\u0441\u0442\u0456 \u0440\u043e\u0431\u0438\u0442\u0438 \u0446\u0435 \u0449\u0435 \u0440\u0430\u0437");
        } else {
            this.reply(origin, "\u0412\u0430\u0448 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u0437\u0430\u0440e\u0454\u0441\u0442\u0440\u043e\u0432\u0430\u043d\u043e \u0443 \u0446\u044c\u043e\u043c\u0443 \u0447\u0430\u0442\u0456!\n\u041f\u0440\u0438\u0454\u043c\u043d\u043e\u0457 \u0435\u043a\u0441\u043f\u043b\u0443\u0430\u0442\u0430\u0446\u0456\u0457, \u0448\u043a\u0456\u0440\u044f\u043d\u0438\u0439 \u043c\u0456\u0448\u043e\u043a!");
            if (this.chatService.find(origin.getChatId()) == null) {
                ReverenceChat reverenceChat = new ReverenceChat();
                reverenceChat.setChatId(origin.getChatId());
                this.chatService.save(reverenceChat);
            }
            ReverenceUser reverenceUser = new ReverenceUser();
            reverenceUser.setUserId(user.getId());
            reverenceUser.setUsername(user.getUserName());
            reverenceUser.setCredits(100);
            reverenceUser.setSustainable(100);
            reverenceUser.setChannel(this.chatService.find(origin.getChatId()));
            this.userService.save(reverenceUser);
        }
    }
}

