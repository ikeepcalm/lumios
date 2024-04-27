package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class QueueMarkupUtil {

    public static InlineKeyboardMarkup createMarkup(SimpleQueue simpleQueue) {
        ;
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton queueUp = new InlineKeyboardButton("Join \uD83D\uDD30");
        queueUp.setCallbackData(simpleQueue.getId() + "-simple-join");

        InlineKeyboardButton exit = new InlineKeyboardButton("Leave \ud83d\udd04");
        exit.setCallbackData(simpleQueue.getId() + "-simple-exit");

        InlineKeyboardButton delete = new InlineKeyboardButton("Delete ❌");
        delete.setCallbackData(simpleQueue.getId() + "-simple-delete");

        InlineKeyboardButton notify = new InlineKeyboardButton("Notify ⚠");
        notify.setCallbackData(simpleQueue.getId() + "-simple-notify");
        firstRow.add(queueUp);
        firstRow.add(exit);

        secondRow.add(delete);
        secondRow.add(notify);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup createMarkup(MixedQueue mixedQueue) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton join = new InlineKeyboardButton("Join \uD83D\uDD30");
        join.setCallbackData(mixedQueue.getId() + "-mixed-join");

        InlineKeyboardButton shuffle = new InlineKeyboardButton("Shuffle \uD83D\uDD00");
        shuffle.setCallbackData(mixedQueue.getId() + "-mixed-shuffle");
        firstRow.add(join);
        secondRow.add(shuffle);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static TextMessage createNotification(long chatId, SimpleQueue simpleQueue) {
        SimpleUser simpleUser = simpleQueue.getContents().get(0);
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setMessageId(simpleQueue.getMessageId());

        if (simpleUser != null) {
            textMessage.setText(simpleUser.getName() + " (@" + simpleUser.getUsername() + "), твоя черга відповідати у " + simpleQueue.getAlias() + "!");
        }

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        InlineKeyboardButton flush = new InlineKeyboardButton("I'm done ✅");
        flush.setCallbackData(simpleQueue.getId() + "-simple-exit");

        row.add(flush);

        keyboard.add(row);

        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));

        return textMessage;
    }

}
