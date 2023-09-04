package dev.ua.ikeepcalm.queueupnow.utils;

import dev.ua.ikeepcalm.queueupnow.entities.Queue;
import dev.ua.ikeepcalm.queueupnow.entities.User;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.MultiMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class DublicateUtil {

    public static InlineKeyboardMarkup createMarkup(Queue queue){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        List<InlineKeyboardButton> thirdRow = new ArrayList<>();
        InlineKeyboardButton queueUp = new InlineKeyboardButton();
        queueUp.setText("Приєднатися \uD83D\uDD3C");
        queueUp.setCallbackData(queue.getId() + "-join");
        InlineKeyboardButton flush = new InlineKeyboardButton();
        flush.setText("Я вже все ✅");
        flush.setCallbackData(queue.getId() + "-flush");
        InlineKeyboardButton exit = new InlineKeyboardButton();
        exit.setText("Вийти \uD83D\uDD04");
        exit.setCallbackData(queue.getId() + "-exit");
        firstRow.add(queueUp);
        secondRow.add(flush);
        thirdRow.add(exit);
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static MultiMessage createNotification(long chatId, Queue queue){
        User user = queue.getContents().peek();
        MultiMessage multiMessage  = new MultiMessage();
        multiMessage.setChatId(chatId);
        multiMessage.setText(user.getName() + " (@" + user.getUsername() + "), твоя черга відповідати у " + queue.getAlias() + "!");
        return multiMessage;
    }

    public static AlterMessage updateMessage(Message origin, Queue queue) {
        AlterMessage generalMessage = new AlterMessage();
        generalMessage.setChatId(origin.getChatId());
        generalMessage.setMessageId((int) queue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (User iteUser : queue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteUser.getName()).append(" (@").append(iteUser.getUsername()).append(")\n");
            id++;
        }
        generalMessage.setText(stringBuilder.toString());
        generalMessage.setReplyKeyboard(DublicateUtil.createMarkup(queue));
        return generalMessage;
    }
}
