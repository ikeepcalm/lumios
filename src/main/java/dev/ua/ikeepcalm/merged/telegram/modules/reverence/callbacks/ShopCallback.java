package dev.ua.ikeepcalm.merged.telegram.modules.reverence.callbacks;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ShopService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ShopCallback extends CommandParent {

    private final ChatService chatService;
    private final UserService userService;
    private final ShopService shopService;

    @Autowired
    public ShopCallback(ChatService chatService, UserService userService, ShopService shopService) {
        this.chatService = chatService;
        this.userService = userService;
        this.shopService = shopService;
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        ShoppingUser whoCalled = shopService.find(origin.getFrom().getId(), chatService.find(origin.getMessage().getChatId()));
        if (whoCalled == null) {
            sendMessage(origin, "@" + origin.getFrom().getUserName() + ", спочатку виконайте /shop@queueupnow_bot!");
            return;
        }
        ReverenceUser user = userService.findById(origin.getFrom().getId(), whoCalled.getChannel());
        int cost = 0;
        int increment = switch (receivedCallback) {
            case "shop_10" -> {
                cost = 100;
                yield 10;
            }
            case "shop_50" -> {
                cost = 500;
                yield 50;
            }
            case "shop_100" -> {
                cost = 1000;
                yield 100;
            }
            default -> 0;
        };




        if (processPurchase(user, whoCalled, cost, increment)) {
            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(origin.getMessage().getChatId());
            editMessage.setMessageId(origin.getMessage().getMessageId());
            editMessage.setText("@" + user.getUsername() + " витратив " + cost + " (✧) і збільшив своє щоденне оновлення на " + increment + "!");
            editMessage(editMessage);
        } else {
            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(origin.getMessage().getChatId());
            editMessage.setMessageId(origin.getMessage().getMessageId());
            editMessage.setText("@" + user.getUsername() + ", у вас недостатньо грошей (✧) у гаманці!\n\nНаявно на балансі: " + user.getBalance() + " (✧)\n\nНеобхідно для цієї дії: " + cost + " (✧)");
            editMessage(editMessage);
        }
    }

    private boolean processPurchase(ReverenceUser user, ShoppingUser shoppingUser, int cost, int increment) {
        if (user.getBalance() >= cost) {
            user.setBalance(user.getBalance() - cost);
            user.setSustainable(user.getSustainable() + increment);
            userService.save(user);
            shopService.save(shoppingUser);
            return true;
        }
        return false;
    }
}
