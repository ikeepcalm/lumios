/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
 */
package dev.ua.ikeepcalm.merged.utils;

import dev.ua.ikeepcalm.merged.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.entities.queue.QueueUser;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import java.util.ArrayList;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class DublicateUtil {
    public static InlineKeyboardMarkup createMarkup(QueueItself queueItself) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList keyboard = new ArrayList();
        ArrayList<InlineKeyboardButton> firstRow = new ArrayList<InlineKeyboardButton>();
        ArrayList<InlineKeyboardButton> secondRow = new ArrayList<InlineKeyboardButton>();
        ArrayList<InlineKeyboardButton> thirdRow = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton queueUp = new InlineKeyboardButton();
        queueUp.setText("\u041f\u0440\u0438\u0454\u0434\u043d\u0430\u0442\u0438\u0441\u044f \ud83d\udd3c");
        queueUp.setCallbackData(queueItself.getId() + "-join");
        InlineKeyboardButton flush = new InlineKeyboardButton();
        flush.setText("\u042f \u0432\u0436\u0435 \u0432\u0441\u0435 \u2705");
        flush.setCallbackData(queueItself.getId() + "-flush");
        InlineKeyboardButton exit = new InlineKeyboardButton();
        exit.setText("\u0412\u0438\u0439\u0442\u0438 \ud83d\udd04");
        exit.setCallbackData(queueItself.getId() + "-exit");
        firstRow.add(queueUp);
        secondRow.add(flush);
        thirdRow.add(exit);
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static MultiMessage createNotification(long chatId, QueueItself queueItself) {
        QueueUser queueUser = queueItself.getContents().peek();
        MultiMessage multiMessage = new MultiMessage();
        multiMessage.setChatId(chatId);
        multiMessage.setText(queueUser.getName() + " (@" + queueUser.getUsername() + "), \u0442\u0432\u043e\u044f \u0447\u0435\u0440\u0433\u0430 \u0432\u0456\u0434\u043f\u043e\u0432\u0456\u0434\u0430\u0442\u0438 \u0443 " + queueItself.getAlias() + "!");
        return multiMessage;
    }

    public static AlterMessage updateMessage(Message origin, QueueItself queueItself) {
        AlterMessage generalMessage = new AlterMessage();
        generalMessage.setChatId(origin.getChatId());
        generalMessage.setMessageId((int)queueItself.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queueItself.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (QueueUser iteQueueUser : queueItself.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteQueueUser.getName()).append(" (@").append(iteQueueUser.getUsername()).append(")\n");
            ++id;
        }
        generalMessage.setText(stringBuilder.toString());
        generalMessage.setReplyKeyboard((ReplyKeyboard)DublicateUtil.createMarkup(queueItself));
        return generalMessage;
    }
}

