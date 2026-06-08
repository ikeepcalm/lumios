package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassMarkupUtil {

    public static TextMessage createNowNotification(ClassEntry classEntry, LumiosChat chat, TranslationService translationService) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());
        textMessage.setText(translationService.getMessage("class.now.notification", chat, determineEmoji(classEntry.getClassType()), classEntry.getName()));

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton(translationService.getMessage("class.button.link", chat));
        if (classEntry.getUrl() == null) {
            notify.setText(translationService.getMessage("class.button.fice", chat));
            notify.setUrl("https://ficeadvisor.com/schedule?week=1");
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton secondButton = new InlineKeyboardButton(translationService.getMessage("class.button.add_link", chat));
            secondButton.setCallbackData("classlink-add-" + classEntry.getId());
            secondRow.add(secondButton);
            keyboard.add(secondRow);
        } else {
            notify.setUrl(classEntry.getUrl());
            // Add remove link button when URL exists
            InlineKeyboardRow removeRow = new InlineKeyboardRow();
            InlineKeyboardButton removeButton = new InlineKeyboardButton(translationService.getMessage("class.button.remove_link", chat));
            removeButton.setCallbackData("classlink-remove-" + classEntry.getId());
            removeRow.add(removeButton);
            keyboard.add(removeRow);
        }
        firstRow.add(notify);
        keyboard.add(firstRow);

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createNextNotification(ClassEntry classEntry, LumiosChat chat, TranslationService translationService) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());
        textMessage.setText(translationService.getMessage("class.next.notification", chat, classEntry.getStartTime(), determineEmoji(classEntry.getClassType()), classEntry.getName()));

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton(translationService.getMessage("class.button.link", chat));
        if (classEntry.getUrl() == null) {
            notify.setText(translationService.getMessage("class.button.fice", chat));
            notify.setUrl("https://ficeadvisor.com/schedule?week=1");
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton secondButton = new InlineKeyboardButton(translationService.getMessage("class.button.add_link", chat));
            secondButton.setCallbackData("classlink-add-" + classEntry.getId());
            secondRow.add(secondButton);
            keyboard.add(secondRow);
        } else {
            notify.setUrl(classEntry.getUrl());
            // Add remove link button when URL exists
            InlineKeyboardRow removeRow = new InlineKeyboardRow();
            InlineKeyboardButton removeButton = new InlineKeyboardButton(translationService.getMessage("class.button.remove_link", chat));
            removeButton.setCallbackData("classlink-remove-" + classEntry.getId());
            removeRow.add(removeButton);
            keyboard.add(removeRow);
        }
        firstRow.add(notify);
        keyboard.add(firstRow);
        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createMultipleNowNotification(List<ClassEntry> classEntries, LumiosChat chat, TranslationService translationService) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());

        String classesText = classEntries.stream()
                .map(classEntry -> determineEmoji(classEntry.getClassType()) + " " + classEntry.getName())
                .collect(Collectors.joining("\n"));

        textMessage.setText(translationService.getMessage("class.multiple.now.notification", chat, classesText));

        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        for (ClassEntry classEntry : classEntries) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton button;

            if (classEntry.getUrl() == null) {
                button = new InlineKeyboardButton(translationService.getMessage("class.button.fice", chat) + " - " + classEntry.getName());
                button.setUrl("https://ficeadvisor.com/schedule?week=1");
            } else {
                button = new InlineKeyboardButton("🌐 " + classEntry.getName());
                button.setUrl(classEntry.getUrl());
            }
            row.add(button);
            keyboard.add(row);

            // Only show "add link" button for multiple classes, not "remove" (to avoid clutter)
            if (classEntry.getUrl() == null) {
                InlineKeyboardRow addLinkRow = new InlineKeyboardRow();
                InlineKeyboardButton addLinkButton = new InlineKeyboardButton(translationService.getMessage("class.button.add_link_for", chat, classEntry.getName()));
                addLinkButton.setCallbackData("classlink-add-" + classEntry.getId());
                addLinkRow.add(addLinkButton);
                keyboard.add(addLinkRow);
            }
        }

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        return textMessage;
    }

    public static TextMessage createMultipleNextNotification(List<ClassEntry> classEntries, LumiosChat chat, TranslationService translationService) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());

        String classesText = classEntries.stream()
                .map(classEntry -> determineEmoji(classEntry.getClassType()) + " " + classEntry.getName())
                .collect(Collectors.joining("\n"));

        textMessage.setText(translationService.getMessage("class.multiple.next.notification", chat, classEntries.get(0).getStartTime(), classesText));

        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        for (ClassEntry classEntry : classEntries) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton button;

            if (classEntry.getUrl() == null) {
                button = new InlineKeyboardButton(translationService.getMessage("class.button.fice", chat) + " - " + classEntry.getName());
                button.setUrl("https://ficeadvisor.com/schedule?week=1");
            } else {
                button = new InlineKeyboardButton("🌐 " + classEntry.getName());
                button.setUrl(classEntry.getUrl());
            }
            row.add(button);
            keyboard.add(row);

            // Only show "add link" button for multiple classes, not "remove" (to avoid clutter)
            if (classEntry.getUrl() == null) {
                InlineKeyboardRow addLinkRow = new InlineKeyboardRow();
                InlineKeyboardButton addLinkButton = new InlineKeyboardButton(translationService.getMessage("class.button.add_link_for", chat, classEntry.getName()));
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
            case "LECTURE" -> "🔵";
            case "PRACTICE" -> "🟠";
            case "LAB" -> "🟢";
            default -> "?";
        };
    }

}
