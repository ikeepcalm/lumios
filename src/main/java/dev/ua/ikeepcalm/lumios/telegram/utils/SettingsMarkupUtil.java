package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class SettingsMarkupUtil {

    public static InlineKeyboardMarkup getSettingsKeyboard(LumiosChat lumiosChat) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton timetableEnabled;
        if (lumiosChat.isTimetableEnabled()) {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ✅");
            timetableEnabled.setCallbackData("settings-timetable-disable");
        } else {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ❌");
            timetableEnabled.setCallbackData("settings-timetable-enable");
        }

        InlineKeyboardRow secondRow = new InlineKeyboardRow();
        InlineKeyboardButton diceEnabled;
        if (lumiosChat.isDiceEnabled()) {
            diceEnabled = new InlineKeyboardButton("Кубики ✅");
            diceEnabled.setCallbackData("settings-dice-disable");
        } else {
            diceEnabled = new InlineKeyboardButton("Кубики ❌");
            diceEnabled.setCallbackData("settings-dice-enable");
        }

        firstRow.add(timetableEnabled);
        secondRow.add(diceEnabled);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }

}
