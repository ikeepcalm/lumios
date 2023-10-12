/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged;

import dev.ua.ikeepcalm.merged.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Component
public class ShopCommand
extends Executable {
    public void execute(Message origin) {
        MultiMessage message = new MultiMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setText("\u041e\u0431\u0435\u0440\u0456\u0442\u044c \u0432\u0430\u0440\u0456\u0430\u043d\u0442 \u0437\u0431\u0456\u043b\u044c\u0448\u0435\u043d\u043d\u044f \u043e\u043d\u043e\u0432\u043b\u044e\u0432\u0430\u043d\u043d\u043e\u0457 \u043f\u043e\u0432\u0430\u0433\u0438!");
        if (this.raiseService.find(origin.getFrom().getId(), this.chatService.find(origin.getChatId())) == null) {
            ShoppingUser shoppingUser = new ShoppingUser();
            shoppingUser.setUserId(origin.getFrom().getId());
            shoppingUser.setChannel(this.chatService.find(origin.getChatId()));
            this.raiseService.save(shoppingUser);
        }
        String[] values = new String[]{"10", "50", "100"};
        String prefix = "increase_";
        InlineKeyboardMarkup inlineKeyboardMarkup = this.telegramService.createMarkup(values, prefix);
        message.setReplyKeyboard((ReplyKeyboard)inlineKeyboardMarkup);
        this.telegramService.sendMultiMessage(message);
    }
}

