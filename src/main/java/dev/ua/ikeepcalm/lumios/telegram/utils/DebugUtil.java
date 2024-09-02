package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DebugUtil {

    public static ReplyKeyboard createGroupsKeyboard(Map<String, String> groups) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int maxButtons = Math.min(groups.size(), 9);

        int count = 0;
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            if (count >= maxButtons) break;
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData("debug#group#" + entry.getKey());
            buttons.add(button);
            count++;
        }

        int rows, columns;
        if (buttons.size() <= 6) {
            rows = 3;
            columns = 2;
        } else {
            rows = 3;
            columns = 3;
        }

        for (int i = 0; i < rows; i++) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j;
                if (index < buttons.size()) {
                    row.add(buttons.get(index));
                }
            }
            keyboard.add(row);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createGroupKeyboard(String chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton notify = new InlineKeyboardButton("Notify");
        notify.setCallbackData("debug#notify#" + chatId);
        row.add(notify);

        InlineKeyboardButton stats = new InlineKeyboardButton("Stats");
        stats.setCallbackData("debug#stats#" + chatId);
        row.add(stats);

        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#back#" + chatId);
        secondRow.add(back);

        keyboard.add(row);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }
}
