package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class StatsCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        List<ReverenceUser> users = userService.findAll(reverenceChat);
        String statsMessage = buildStatsMessage(users);
        sendMessage(statsMessage, ParseMode.MARKDOWN);
    }

    private String buildStatsMessage(List<ReverenceUser> users) {
        List<ReverenceUser> sortedUsers = users.stream()
                .sorted((user1, user2) -> Integer.compare(user2.getReverence(), user1.getReverence()))
                .toList();

        StringBuilder builder = new StringBuilder("```Загальна-статистика");

        for (ReverenceUser user : sortedUsers) {
            builder.append(" ▻ ").append(user.getUsername()).append(": ").append(user.getReverence()).append("\n");
        }

        builder.append("```");

        return builder.toString();
    }


}
