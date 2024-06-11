package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class SettingsMarkupUtil {

    public static InlineKeyboardMarkup getSettingsKeyboard(ReverenceChat reverenceChat) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton timetableEnabled;
        if (reverenceChat.isTimetableEnabled()) {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ✅");
            timetableEnabled.setCallbackData("settings-timetable-disable");
        } else {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ❌");
            timetableEnabled.setCallbackData("settings-timetable-enable");
        }
        firstRow.add(timetableEnabled);

        keyboard.add(firstRow);

        return new InlineKeyboardMarkup(keyboard);
    }

}
