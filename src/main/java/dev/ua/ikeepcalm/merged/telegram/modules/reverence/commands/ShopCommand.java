package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class ShopCommand
        extends CommandParent {
    public void execute(Message origin) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setText("Оберіть варіант збільшення постійних кредитів!");
        if (this.shopService.find(origin.getFrom().getId(), this.chatService.find(origin.getChatId())) == null) {
            ShoppingUser shoppingUser = new ShoppingUser();
            shoppingUser.setUserId(origin.getFrom().getId());
            shoppingUser.setChannel(this.chatService.find(origin.getChatId()));
            this.shopService.save(shoppingUser);
        }
        String[] values = new String[]{"10", "50", "100"};
        String prefix = "shop_";
        InlineKeyboardMarkup inlineKeyboardMarkup = absSender.createMarkup(values, prefix);
        message.setReplyKeyboard(inlineKeyboardMarkup);
        sendMessage(origin, message);
    }
}

