package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@BotCommand(command = "stats")
public class StatsCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        List<LumiosUser> users = userService.findAll(chat);
        TextMessage message = new TextMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText(buildStatsMessage(users));
        message.setParseMode(ParseMode.MARKDOWN);

        if (users.size() > 10) {
            message.setReplyKeyboard(buildStatsKeyboard());
        }

        sendMessage(message, update.getMessage());
    }

    private String buildStatsMessage(List<LumiosUser> users) {
        List<LumiosUser> sortedUsers = users.stream()
                .sorted((user1, user2) -> Integer.compare(user2.getReverence(), user1.getReverence()))
                .toList();

        StringBuilder builder = new StringBuilder("```Статистика⠀(1/" + (sortedUsers.size() / 10 + 1) + ")\n\n");

        int count = 0;
        for (LumiosUser user : sortedUsers) {
            if (count == 10) {
                break;
            }
            builder.append(" ﹥ ").append(user.getUsername()).append(": ").append(user.getReverence()).append("\n");
            count++;
        }

        builder.append("```");

        return builder.toString();
    }

    private InlineKeyboardMarkup buildStatsKeyboard() {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        forward.setCallbackData("stats-1-forward");
        firstRow.add(forward);
        keyboard.add(firstRow);
        return new InlineKeyboardMarkup(keyboard);
    }

}