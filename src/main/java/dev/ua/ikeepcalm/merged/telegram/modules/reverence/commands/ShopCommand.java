package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ShopService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class ShopCommand extends CommandParent {

    private final ChatService chatService;
    private final ShopService shopService;

    public ShopCommand(ChatService chatService,    ShopService shopService) {
        this.chatService = chatService;
        this.shopService = shopService;
    }

    public void execute(Message origin) {
        long chatId = origin.getChatId();
        long userId = origin.getFrom().getId();
        int messageId = origin.getMessageId();
        TextMessage message = createShopMessage(chatId, messageId);

        if (shopService.find(userId, chatService.find(chatId)) == null) {
            ShoppingUser shoppingUser = new ShoppingUser();
            shoppingUser.setUserId(userId);
            shoppingUser.setChannel(chatService.find(chatId));
            shopService.save(shoppingUser);
        }

        String[] values = new String[]{"10", "50", "100"};
        String prefix = "shop_";
        InlineKeyboardMarkup inlineKeyboardMarkup = absSender.createMarkup(values, prefix);
        message.setReplyKeyboard(inlineKeyboardMarkup);
        sendMessage(origin, message);
    }

    private TextMessage createShopMessage(long chatId, int messageId) {
        TextMessage message = new TextMessage();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Оберіть варіант збільшення постійних кредитів!");
        return message;
    }
}
