package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.timetable.commands;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeedCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText("Посилання на онлайн-редактор розкладу ⬇️");
        textMessage.setMessageId(message.getMessageId());

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton("\uD83C\uDF10 Онлайн портал");
        notify.setUrl("https://timetable.uaproject.xyz/timetable?chatId=" + message.getChatId());
        firstRow.add(notify);
        keyboard.add(firstRow);
        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        sendMessage(textMessage);
    }
}

