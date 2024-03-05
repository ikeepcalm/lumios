package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.timetable.utils;

import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ClassMarkupUtil {

    public static TextMessage createNowNotification(ClassEntry classEntry, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                + "Шановне панство, незабаром почнеться / вже проходить пара: \n"
                + determineEmoji(classEntry.getClassType()) + " " + classEntry.getName() + "\n\n"
                + "Посилання на конференцію ⬇️"
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        ArrayList<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton notify = new InlineKeyboardButton();
        notify.setText("\uD83C\uDF10 Туда мені нада");
        notify.setUrl(classEntry.getUrl());
        firstRow.add(notify);
        keyboard.add(firstRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        textMessage.setReplyKeyboard(inlineKeyboardMarkup);
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return  textMessage;
    }

    public static TextMessage createNextNotification(ClassEntry classEntry, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                + "Наступна пара починається о " + classEntry.getStartTime() + " і має назву:" + "\n"
                + determineEmoji(classEntry.getClassType()) + " " + classEntry.getName() + "\n\n"
                + "Посилання на конференцію ⬇️"
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        ArrayList<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton notify = new InlineKeyboardButton();
        notify.setText("\uD83C\uDF10 Туда мені нада");
        notify.setUrl(classEntry.getUrl());
        firstRow.add(notify);
        keyboard.add(firstRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        textMessage.setReplyKeyboard(inlineKeyboardMarkup);
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return  textMessage;
    }

    private static String determineEmoji(ClassType classType){
        return switch (classType.name()){
            case "LECTURE" -> "\uD83D\uDD35";
            case "PRACTICE" -> "\uD83D\uDFE0";
            case "LAB" -> "\uD83D\uDFE2";
            default -> "?";
        };
    }

}
