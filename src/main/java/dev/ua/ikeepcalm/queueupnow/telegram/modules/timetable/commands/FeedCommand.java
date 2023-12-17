package dev.ua.ikeepcalm.queueupnow.telegram.modules.timetable.commands;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeedCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText("Посилання на онлайн-редактор розкладу ⬇️");
        textMessage.setMessageId(message.getMessageId());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        ArrayList<InlineKeyboardButton> firstRow = new ArrayList<>();
        InlineKeyboardButton notify = new InlineKeyboardButton();
        notify.setText("\uD83C\uDF10 Онлайн редактор");
        notify.setUrl("https://timetable.uaproject.xyz/timetable?chatId=" + message.getChatId());
        firstRow.add(notify);
        keyboard.add(firstRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        textMessage.setReplyKeyboard(inlineKeyboardMarkup);
        sendMessage(textMessage);
    }
}

