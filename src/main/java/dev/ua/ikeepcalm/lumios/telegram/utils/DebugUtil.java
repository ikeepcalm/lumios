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
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        InlineKeyboardRow row2 = new InlineKeyboardRow();
        InlineKeyboardRow row3 = new InlineKeyboardRow();

        InlineKeyboardButton notify = new InlineKeyboardButton("Notify");
        notify.setCallbackData("debug#notify#" + chatId);
        row1.add(notify);

        InlineKeyboardButton stats = new InlineKeyboardButton("Stats");
        stats.setCallbackData("debug#stats#" + chatId);
        row1.add(stats);

        InlineKeyboardButton users = new InlineKeyboardButton("Users");
        users.setCallbackData("debug#users#" + chatId);
        row2.add(users);

        InlineKeyboardButton settings = new InlineKeyboardButton("Settings");
        settings.setCallbackData("debug#settings#" + chatId);
        row2.add(settings);

        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#back#" + chatId);
        row3.add(back);

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createUsersKeyboard(Map<Long, String> users, String chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int maxButtons = Math.min(users.size(), 8);
        int count = 0;
        
        for (Map.Entry<Long, String> entry : users.entrySet()) {
            if (count >= maxButtons) break;
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData("debug#user#" + chatId + "#" + entry.getKey());
            buttons.add(button);
            count++;
        }

        int columns = Math.min(buttons.size(), 2);
        int rows = (int) Math.ceil((double) buttons.size() / columns);

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

        InlineKeyboardRow backRow = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#group#" + chatId);
        backRow.add(back);
        keyboard.add(backRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createUserManagementKeyboard(String chatId, Long userId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        InlineKeyboardButton setReverence = new InlineKeyboardButton("+100 Rev");
        setReverence.setCallbackData("debug#setrev#" + chatId + "#" + userId + "#100");
        row1.add(setReverence);
        
        InlineKeyboardButton removeReverence = new InlineKeyboardButton("-100 Rev");
        removeReverence.setCallbackData("debug#setrev#" + chatId + "#" + userId + "#-100");
        row1.add(removeReverence);
        
        InlineKeyboardRow row2 = new InlineKeyboardRow();
        InlineKeyboardButton resetReverence = new InlineKeyboardButton("Reset Rev");
        resetReverence.setCallbackData("debug#resetrev#" + chatId + "#" + userId);
        row2.add(resetReverence);
        
        InlineKeyboardRow row3 = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#users#" + chatId);
        row3.add(back);
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createSettingsKeyboard(String chatId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        InlineKeyboardButton summaryLimit = new InlineKeyboardButton("Summary Limit");
        summaryLimit.setCallbackData("debug#summary#" + chatId);
        row1.add(summaryLimit);
        
        InlineKeyboardButton commLimit = new InlineKeyboardButton("Comm Limit");
        commLimit.setCallbackData("debug#comm#" + chatId);
        row1.add(commLimit);
        
        InlineKeyboardRow row2 = new InlineKeyboardRow();
        InlineKeyboardButton aiToggle = new InlineKeyboardButton("Toggle AI");
        aiToggle.setCallbackData("debug#toggleai#" + chatId);
        row2.add(aiToggle);
        
        InlineKeyboardRow row3 = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#group#" + chatId);
        row3.add(back);
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createLimitKeyboard(String chatId, String type) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        InlineKeyboardButton plus1 = new InlineKeyboardButton("+1");
        plus1.setCallbackData("debug#limit#" + chatId + "#" + type + "#+1");
        row1.add(plus1);
        
        InlineKeyboardButton plus5 = new InlineKeyboardButton("+5");
        plus5.setCallbackData("debug#limit#" + chatId + "#" + type + "#+5");
        row1.add(plus5);
        
        InlineKeyboardButton plus10 = new InlineKeyboardButton("+10");
        plus10.setCallbackData("debug#limit#" + chatId + "#" + type + "#+10");
        row1.add(plus10);
        
        InlineKeyboardRow row2 = new InlineKeyboardRow();
        InlineKeyboardButton minus1 = new InlineKeyboardButton("-1");
        minus1.setCallbackData("debug#limit#" + chatId + "#" + type + "#-1");
        row2.add(minus1);
        
        InlineKeyboardButton minus5 = new InlineKeyboardButton("-5");
        minus5.setCallbackData("debug#limit#" + chatId + "#" + type + "#-5");
        row2.add(minus5);
        
        InlineKeyboardButton minus10 = new InlineKeyboardButton("-10");
        minus10.setCallbackData("debug#limit#" + chatId + "#" + type + "#-10");
        row2.add(minus10);
        
        InlineKeyboardRow row3 = new InlineKeyboardRow();
        InlineKeyboardButton back = new InlineKeyboardButton("Back");
        back.setCallbackData("debug#settings#" + chatId);
        row3.add(back);
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        return new InlineKeyboardMarkup(keyboard);
    }
}
