package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatsCommand extends CommandParent {

    private final UserService userService;
    private final ChatService chatService;

    public StatsCommand(UserService userService, ChatService chatService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    public void execute(Message origin) {
        ReverenceChat linkedChat = chatService.find(origin.getChatId());
        List<ReverenceUser> users = userService.findAll(linkedChat);

        String statsMessage = buildStatsMessage(users);
        reply(origin, ParseMode.MARKDOWN, statsMessage);
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
