package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class PagedUtil {

    public static String buildStatsMessage(List<LumiosUser> users, int page) {
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

    public static InlineKeyboardMarkup buildStatsKeyboard(int page, int maxPage, boolean exit, char split, long chatId) {
        if (page == 1) {
            return buildFirstPageKeyboard(page, exit, split, chatId);
        } else if (page == maxPage) {
            return buildLastPageKeyboard(page, exit, split, chatId);
        } else {
            return buildMiddlePageKeyboard(page, exit, split, chatId);
        }
    }

    private static InlineKeyboardMarkup buildFirstPageKeyboard(int page, boolean exit, char split, long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        forward.setCallbackData("stats" + split + page + split + "forward" + split + chatId);
        firstRow.add(forward);
        keyboard.add(firstRow);
        if (exit) {
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton backButton = new InlineKeyboardButton("❌");
            backButton.setCallbackData("stats" + split + page + split + "exit" + split + chatId);
            secondRow.add(backButton);
            keyboard.add(secondRow);
        }
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup buildMiddlePageKeyboard(int page, boolean exit, char split, long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        back.setCallbackData("stats" + split + page + split + "back" + split + chatId);
        forward.setCallbackData("stats" + split + page + split + "forward" + split + chatId);
        firstRow.add(back);
        firstRow.add(forward);
        keyboard.add(firstRow);
        if (exit) {
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton backButton = new InlineKeyboardButton("❌");
            backButton.setCallbackData("stats" + split + page + split + "exit" + split + chatId);
            secondRow.add(backButton);
            keyboard.add(secondRow);
        }
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup buildLastPageKeyboard(int page, boolean exit, char split, long chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("⬅️");
        back.setCallbackData("stats" + split + page + split + "back" + split + chatId);
        firstRow.add(back);
        keyboard.add(firstRow);
        if (exit) {
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton backButton = new InlineKeyboardButton("❌");
            backButton.setCallbackData("stats" + split + page + split + "exit" + split + chatId);
            secondRow.add(backButton);
            keyboard.add(secondRow);
        }
        return new InlineKeyboardMarkup(keyboard);
    }

}
