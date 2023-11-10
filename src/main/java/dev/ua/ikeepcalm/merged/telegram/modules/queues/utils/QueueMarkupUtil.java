package dev.ua.ikeepcalm.merged.telegram.modules.queues.utils;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.database.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QueueMarkupUtil {

    public static InlineKeyboardMarkup createMarkup(QueueItself queueItself) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        ArrayList<InlineKeyboardButton> firstRow = new ArrayList<>();
        ArrayList<InlineKeyboardButton> secondRow = new ArrayList<>();

        InlineKeyboardButton queueUp = new InlineKeyboardButton();
        queueUp.setText("Join \uD83D\uDD30");
        queueUp.setCallbackData(queueItself.getId() + "-join");

        InlineKeyboardButton flush = new InlineKeyboardButton();
        flush.setText("I'm done ✅");
        flush.setCallbackData(queueItself.getId() + "-flush");

        InlineKeyboardButton exit = new InlineKeyboardButton();
        exit.setText("Leave \ud83d\udd04");
        exit.setCallbackData(queueItself.getId() + "-exit");

        InlineKeyboardButton delete = new InlineKeyboardButton();
        delete.setText("Delete ❌");
        delete.setCallbackData(queueItself.getId() + "-delete");

        InlineKeyboardButton notify = new InlineKeyboardButton();
        notify.setText("Notify ⚠");
        notify.setCallbackData(queueItself.getId() + "-notify");
        firstRow.add(queueUp);
        firstRow.add(flush);
        firstRow.add(exit);

        secondRow.add(delete);
        secondRow.add(notify);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static TextMessage createNotification(long chatId, QueueItself queueItself) {
        QueueUser queueUser = queueItself.getContents().peek();
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setMessageId(queueItself.getMessageId());

        if (queueUser != null) {
            textMessage.setText(queueUser.getName() + " (@" + queueUser.getUsername() + "), твоя черга відповідати у " + queueItself.getAlias() + "!");
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        ArrayList<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton flush = new InlineKeyboardButton();
        flush.setText("I'm done ✅");
        flush.setCallbackData(queueItself.getId() + "-flush");

        row.add(flush);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        textMessage.setReplyKeyboard(inlineKeyboardMarkup);

        return textMessage;
    }


    public static EditMessage updateMessage(Message message, QueueItself queueItself) {
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(queueItself.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        editMessage.setText(stringBuilder.toString());
        editMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queueItself));
        return editMessage;
    }
}

