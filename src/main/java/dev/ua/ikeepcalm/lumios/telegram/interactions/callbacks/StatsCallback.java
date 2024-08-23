package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@BotCallback(startsWith = "stats-")
public class StatsCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String data = message.getData();
        List<LumiosUser> users = userService.findAll(chat);
        String[] split = data.split("-");
        int page = Integer.parseInt(split[1]);
        int maxPage;
        if (users.size() % 10 == 0) {
            maxPage = users.size() / 10;
        } else {
            maxPage = users.size() / 10 + 1;
        }
        String direction = split[2];

        if (direction.equals("forward")) {
            page++;
        } else if (direction.equals("back")) {
            page--;
        }

        EditMessage editedMessage = new EditMessage();
        editedMessage.setChatId(message.getMessage().getChatId());
        editedMessage.setMessageId(message.getMessage().getMessageId());
        editedMessage.setParseMode(ParseMode.MARKDOWN);
        editedMessage.setText(buildStatsMessage(users, page));
        editedMessage.setReplyKeyboard(buildStatsKeyboard(page, maxPage));
        editMessage(editedMessage);
    }

    private String buildStatsMessage(List<LumiosUser> users, int page) {
        List<LumiosUser> sortedUsers = users.stream()
                .sorted((user1, user2) -> Integer.compare(user2.getReverence(), user1.getReverence()))
                .toList();

        StringBuilder builder = new StringBuilder("```Статистика⠀(" + page + "/" + (sortedUsers.size() / 10 + 1) + ")\n\n");

        int count = 0;
        for (int i = (page - 1) * 10; i < sortedUsers.size(); i++) {
            if (count == 10) {
                break;
            }
            LumiosUser user = sortedUsers.get(i);
            builder.append(" ﹥ ").append(user.getUsername()).append(": ").append(user.getReverence()).append("\n");
            count++;
        }
        builder.append("```");

        return builder.toString();
    }


    private InlineKeyboardMarkup buildStatsKeyboard(int page, int maxPage) {
        if (page == 1) {
            return buildFirstPageKeyboard(page);
        } else if (page == maxPage) {
            return buildLastPageKeyboard(page);
        } else {
            return buildMiddlePageKeyboard(page);
        }
    }

    private InlineKeyboardMarkup buildFirstPageKeyboard(int page) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        forward.setCallbackData("stats-" + page + "-forward");
        firstRow.add(forward);
        keyboard.add(firstRow);
        return new InlineKeyboardMarkup(keyboard);
    }

    private InlineKeyboardMarkup buildMiddlePageKeyboard(int page) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        back.setCallbackData("stats-" + page + "-back");
        forward.setCallbackData("stats-" + page + "-forward");
        firstRow.add(back);
        firstRow.add(forward);
        keyboard.add(firstRow);
        return new InlineKeyboardMarkup(keyboard);
    }

    private InlineKeyboardMarkup buildLastPageKeyboard(int page) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
        back.setCallbackData("stats-" + page + "-back");
        firstRow.add(back);
        keyboard.add(firstRow);
        return new InlineKeyboardMarkup(keyboard);
    }
}
