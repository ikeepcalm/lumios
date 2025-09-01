package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassMarkupUtil {

    public static TextMessage createNowNotification(ClassEntry classEntry, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                            + "Шановне панство, незабаром почнеться / вже проходить пара: \n"
                            + determineEmoji(classEntry.getClassType()) + " " + classEntry.getName() + "\n\n"
                            + "Посилання на конференцію ⬇️"
        );

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton("\uD83C\uDF10 Туда нам нада");
        if (classEntry.getUrl() == null) {
            notify.setText("Fice Advisor");
            notify.setUrl("https://ficeadvisor.com/schedule?week=1");
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton secondButton = new InlineKeyboardButton("Додати посилання \uD83D\uDD17");
            secondButton.setCallbackData("classlink-add-" + classEntry.getId());
            secondRow.add(secondButton);
            keyboard.add(secondRow);
        } else {
            notify.setUrl(classEntry.getUrl());
        }
        firstRow.add(notify);
        keyboard.add(firstRow);

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createNextNotification(ClassEntry classEntry, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                            + "Наступна пара починається о " + classEntry.getStartTime() + " і має назву:" + "\n"
                            + determineEmoji(classEntry.getClassType()) + " " + classEntry.getName() + "\n\n"
                            + "Посилання на конференцію ⬇️"
        );


        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton("\uD83C\uDF10 Туда нам нада");
        if (classEntry.getUrl() == null) {
            notify.setText("Fice Advisor");
            notify.setUrl("https://ficeadvisor.com/schedule?week=1");
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton secondButton = new InlineKeyboardButton("Додати посилання \uD83D\uDD17");
            secondButton.setCallbackData("classlink-add-" + classEntry.getId());
            secondRow.add(secondButton);
            keyboard.add(secondRow);
        } else {
            notify.setUrl(classEntry.getUrl());
        }
        firstRow.add(notify);
        keyboard.add(firstRow);
        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createMultipleNowNotification(List<ClassEntry> classEntries, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        
        String classesText = classEntries.stream()
                .map(classEntry -> determineEmoji(classEntry.getClassType()) + " " + classEntry.getName())
                .collect(Collectors.joining("\n"));
        
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                            + "Шановне панство, незабаром почнуться / вже проходять пари: \n"
                            + classesText + "\n\n"
                            + "Посилання на конференції ⬇️"
        );

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        
        for (ClassEntry classEntry : classEntries) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton button;
            
            if (classEntry.getUrl() == null) {
                button = new InlineKeyboardButton("Fice Advisor - " + classEntry.getName());
                button.setUrl("https://ficeadvisor.com/schedule?week=1");
            } else {
                button = new InlineKeyboardButton("\uD83C\uDF10 " + classEntry.getName());
                button.setUrl(classEntry.getUrl());
            }
            row.add(button);
            keyboard.add(row);
            
            if (classEntry.getUrl() == null) {
                InlineKeyboardRow addLinkRow = new InlineKeyboardRow();
                InlineKeyboardButton addLinkButton = new InlineKeyboardButton("Додати посилання для " + classEntry.getName() + " \uD83D\uDD17");
                addLinkButton.setCallbackData("classlink-add-" + classEntry.getId());
                addLinkRow.add(addLinkButton);
                keyboard.add(addLinkRow);
            }
        }

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createMultipleNextNotification(List<ClassEntry> classEntries, Long chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        
        String classesText = classEntries.stream()
                .map(classEntry -> determineEmoji(classEntry.getClassType()) + " " + classEntry.getName())
                .collect(Collectors.joining("\n"));
        
        textMessage.setText("\uD83D\uDD14 > *НАГАДУВАННЯ* < \uD83D\uDD14\n\n"
                            + "Наступні пари починаються о " + classEntries.get(0).getStartTime() + " і мають назви:" + "\n"
                            + classesText + "\n\n"
                            + "Посилання на конференції ⬇️"
        );

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        
        for (ClassEntry classEntry : classEntries) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton button;
            
            if (classEntry.getUrl() == null) {
                button = new InlineKeyboardButton("Fice Advisor - " + classEntry.getName());
                button.setUrl("https://ficeadvisor.com/schedule?week=1");
            } else {
                button = new InlineKeyboardButton("\uD83C\uDF10 " + classEntry.getName());
                button.setUrl(classEntry.getUrl());
            }
            row.add(button);
            keyboard.add(row);
            
            if (classEntry.getUrl() == null) {
                InlineKeyboardRow addLinkRow = new InlineKeyboardRow();
                InlineKeyboardButton addLinkButton = new InlineKeyboardButton("Додати посилання для " + classEntry.getName() + " \uD83D\uDD17");
                addLinkButton.setCallbackData("classlink-add-" + classEntry.getId());
                addLinkRow.add(addLinkButton);
                keyboard.add(addLinkRow);
            }
        }

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    private static String determineEmoji(ClassType classType) {
        return switch (classType.name()) {
            case "LECTURE" -> "\uD83D\uDD35";
            case "PRACTICE" -> "\uD83D\uDFE0";
            case "LAB" -> "\uD83D\uDFE2";
            default -> "?";
        };
    }

}
