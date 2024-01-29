package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;


@Component
public class ShopCommand extends CommandParent {
    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        TextMessage textMessage = createShopMessage(super.message.getChatId(), super.message.getMessageId());
        String[] values = new String[]{"10", "50", "100"};
        String prefix = "shop_";
        InlineKeyboardMarkup inlineKeyboardMarkup = absSender.createMarkup(values, prefix);
        textMessage.setReplyKeyboard(inlineKeyboardMarkup);
        sendMessage(textMessage);
        ShoppingUser shoppingUser = new ShoppingUser();
        shoppingUser.setChannel(reverenceChat);
        shoppingUser.setUserId(reverenceUser.getUserId());
        shoppingUser.setUserEntityId(reverenceUser.getUserEntityId());
        shopService.save(shoppingUser);
    }

    private TextMessage createShopMessage(long chatId, int messageId) {
        TextMessage message = new TextMessage();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Оберіть варіант збільшення постійних кредитів!");
        return message;
    }

}