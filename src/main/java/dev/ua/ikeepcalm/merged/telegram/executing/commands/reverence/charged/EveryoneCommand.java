/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class EveryoneCommand
extends Executable {
    public void execute(Message origin) {
        ReverenceChat linkedChatId = this.chatService.find(origin.getChatId());
        List<ReverenceUser> usersToPing = this.userService.findAll(linkedChatId);
        ReverenceUser userWhoPings = this.userService.findById(origin.getFrom().getId(), linkedChatId);
        if (userWhoPings.getBalance() >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ReverenceUser instance : usersToPing) {
                stringBuilder.append("@").append(instance.getUsername()).append(" ");
            }
            this.sendAndEditMessage(linkedChatId.getChatId(), stringBuilder.toString(), "@" + userWhoPings.getUsername() + " \u043f\u043e\u043a\u043b\u0438\u043a\u0430\u0432 \u0432\u0430\u0441 \u0434\u043e \u0447\u0430\u0442\u0443!", 12000);
            userWhoPings.setBalance(userWhoPings.getBalance() - 10);
            this.userService.save(userWhoPings);
        } else {
            this.reply(origin, "\u041d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u0433\u0440\u043e\u0448\u0435\u0439 \u0443 \u0433\u0430\u043c\u0430\u043d\u0446\u0456 \u2727!\n\u041d\u0430\u044f\u0432\u043d\u043e \u043d\u0430 \u0431\u0430\u043b\u0430\u043d\u0441\u0456: " + userWhoPings.getBalance() + "\u2727\n\u041d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e \u0434\u043b\u044f \u0446\u0456\u0454\u0457 \u0434\u0456\u0457: 10\u2727");
        }
    }
}

