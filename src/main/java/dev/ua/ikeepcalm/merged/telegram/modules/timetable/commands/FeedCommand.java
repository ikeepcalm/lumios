package dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands;

import dev.ua.ikeepcalm.merged.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.merged.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.utils.TimetableParser;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FeedCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        if (message.isReply()) {
            TimetableEntry timeTable;
            String messageText = message.getReplyToMessage().getText();
            try {
                timeTable = TimetableParser.parseTimetableMessage(messageText);
                timeTable.setChat(reverenceChat);
                try {
                    TimetableEntry foundTimetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(), timeTable.getWeekType());
                    sendMessage("Розклад для цього тижня вже існує! Він буде перезаписаний!");
                    timetableService.delete(foundTimetableEntry);
                    timetableService.save(timeTable);
                } catch (NoSuchEntityException e) {
                    sendMessage("Розклад для цього тижня успішно збережений!");
                    timetableService.save(timeTable);
                }
            } catch (IOException e) {
                sendMessage("Щось пішло не так! Перевірте структуру повідомлення!");
                throw new RuntimeException(e);
            }
        } else {
            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText("Посилання на онлайн-редактор розкладу ⬇️");
            textMessage.setMessageId(message.getMessageId());
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            ArrayList<InlineKeyboardButton> firstRow = new ArrayList<>();
            InlineKeyboardButton notify = new InlineKeyboardButton();
            notify.setText("\uD83C\uDF10 Онлайн редактор");
            notify.setUrl("https://timetable.uaproject.xyz/?chatId=" + message.getChatId());
            firstRow.add(notify);
            keyboard.add(firstRow);
            inlineKeyboardMarkup.setKeyboard(keyboard);
            textMessage.setReplyKeyboard(inlineKeyboardMarkup);
            sendMessage(textMessage);
        }
    }
}

