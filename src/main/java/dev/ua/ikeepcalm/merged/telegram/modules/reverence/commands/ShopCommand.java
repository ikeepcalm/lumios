/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class ShopCommand
extends Executable {
    public void execute(Message origin) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setText("Оберіть варіант збільшення оновлюванної поваги!");
        if (this.shopService.find(origin.getFrom().getId(), this.chatService.find(origin.getChatId())) == null) {
            ShoppingUser shoppingUser = new ShoppingUser();
            shoppingUser.setUserId(origin.getFrom().getId());
            shoppingUser.setChannel(this.chatService.find(origin.getChatId()));
            this.shopService.save(shoppingUser);
        }
        String[] values = new String[]{"10", "50", "100"};
        String prefix = "increase_";
        InlineKeyboardMarkup inlineKeyboardMarkup = this.absSender.createMarkup(values, prefix);
        message.setReplyKeyboard(inlineKeyboardMarkup);
        this.absSender.sendTextMessage(message);
    }
}

