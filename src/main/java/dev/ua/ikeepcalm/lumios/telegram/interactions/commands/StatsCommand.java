package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@BotCommand(command = "stats")
public class StatsCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        List<LumiosUser> users = userService.findAll(chat);
        String statsMessage = buildStatsMessage(users);
        sendMessage(statsMessage, ParseMode.MARKDOWN, update.getMessage());
    }

    private String buildStatsMessage(List<LumiosUser> users) {
        List<LumiosUser> sortedUsers = users.stream()
                .sorted((user1, user2) -> Integer.compare(user2.getReverence(), user1.getReverence()))
                .toList();

        int maxReverence = sortedUsers.getFirst().getReverence();

        StringBuilder builder = new StringBuilder("```Загальна-статистика");

        for (LumiosUser user : sortedUsers) {
            if (user.getReverence() >= maxReverence * 0.01) {
                builder.append(" ▻ ").append(user.getUsername()).append(": ").append(user.getReverence()).append("\n");
            }
        }

        builder.append("```");

        return builder.toString();
    }


}