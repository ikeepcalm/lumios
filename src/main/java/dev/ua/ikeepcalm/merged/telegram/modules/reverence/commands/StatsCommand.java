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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class StatsCommand extends Executable {
    public void execute(Message origin) {
        ReverenceChat linkedChatId = this.chatService.find(origin.getChatId());
        List<ReverenceUser> userEntities = this.userService.findAll(linkedChatId);
        userEntities.sort(Comparator.comparingInt(ReverenceUser::getReverence));
        Collections.reverse(userEntities);
        StringBuilder builder = new StringBuilder();
        for (ReverenceUser reverenceUser : userEntities) {
            builder.append("▻ ").append(reverenceUser.getUsername()).append(": ").append(reverenceUser.getReverence()).append("\n");
        }
        String allResults = builder.toString();
        reply(origin, "↣ Статистика чату ↢\n――――――――――\n\n" + allResults + "\n――――――――――\n↣ Статистика чату ↢");
    }
}

