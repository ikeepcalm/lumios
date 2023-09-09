/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class ForceJoinCommand
extends Executable {
    public void execute(Message origin) {
        User user = origin.getFrom();
        User repliedUser = origin.getReplyToMessage().getFrom();
        if (user.getId() == 586319182L) {
            if (this.userService.checkIfUserExists(origin.getReplyToMessage().getFrom().getId(), this.chatService.find(origin.getChatId()))) {
                this.reply(origin, "\u0414\u0443\u0440\u0435\u043d\u044c, \u0446\u0435\u0439 \u0448\u043a\u0456\u0440\u044f\u043d\u0438\u0439 \u043c\u0456\u0448\u043e\u043a \u0432\u0436\u0435 \u0454 \u0432 \u043d\u0430\u0448\u043e\u043c\u0443 \u0441\u043f\u0438\u0441\u043a\u0443. \u0422\u0438 \u0432\u0437\u0430\u0433\u0430\u043b\u0456 \u0441\u043b\u0456\u0434\u043a\u0443\u0454\u0448 \u0437\u0430 \u0440\u043e\u0431\u043e\u0442\u043e\u044e \u0441\u0432\u043e\u0433\u043e \u0442\u0432\u043e\u0440\u0456\u043d\u043d\u044f?");
            } else {
                this.reply(origin, "\u0429\u043e \u0432\u0438\u0437\u043d\u0430\u0447\u0430\u0454 \u0434\u043e\u043b\u044e \u043b\u044e\u0434\u0438\u043d\u0438 \u0443 \u0446\u044c\u043e\u043c\u0443 \u0441\u0432\u0456\u0442\u0456? \u042f\u043a\u0430\u0441\u044c \u043d\u0435\u0437\u0440\u0438\u043c\u0430 \u0456\u0441\u0442\u043e\u0442\u0430, \u0437\u0430\u043a\u043e\u043d \u0447\u0438 \u043c\u043e\u0436\u0435 \u0442\u0430\u0454\u043c\u043d\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430? \u0426\u0435\u0439 \u0448\u043a\u0456\u0440\u044f\u043d\u0438\u0439 \u043c\u0456\u0448\u043e\u043a \u0431\u0443\u0432 \u043d\u0430\u0441\u0438\u043b\u044c\u043d\u043e \u0443\u0432\u0435\u0434\u0435\u043d\u0438\u0439 \u0434\u043e \u0411\u0414 :D");
                ReverenceUser reverenceUser = new ReverenceUser();
                reverenceUser.setUserId(repliedUser.getId());
                reverenceUser.setUsername(repliedUser.getUserName());
                reverenceUser.setCredits(100);
                reverenceUser.setSustainable(100);
                reverenceUser.setChannel(this.chatService.find(origin.getChatId()));
                this.userService.save(reverenceUser);
            }
        } else {
            this.telegramService.sendVideo(origin.getChatId(), "src/main/resources/crazy.mp4", origin.getMessageId());
        }
    }
}

