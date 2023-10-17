/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
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
            this.sendAndEditMessage(linkedChatId.getChatId(), stringBuilder.toString(), "@" + userWhoPings.getUsername() + " покликав вас до чату!");
            userWhoPings.setBalance(userWhoPings.getBalance() - 10);
            this.userService.save(userWhoPings);
        } else {
            this.reply(origin, "Недостатньо грошей у гаманці ✧!\nНаявно на балансі: " + userWhoPings.getBalance() + "✧\nНеобхідно для цієї дії: 10✧");
        }
    }
}

