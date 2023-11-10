package dev.ua.ikeepcalm.merged.telegram.modules.reverence.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ShopCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData();
        instantiateUpdate(message);
        ShoppingUser whoCalled = shopService.find(message.getFrom().getId(), reverenceChat);

        if (whoCalled == null) {
            sendMessage("@" + super.message.getFrom().getUserName() + ", спочатку виконайте /shop@queueupnow_bot!");
            return;
        }
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


        if (processPurchase(reverenceUser, whoCalled, cost, increment)) {
            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(super.message.getChatId());
            editMessage.setMessageId(super.message.getMessageId());
            editMessage.setText("@" + reverenceUser.getUsername() + " витратив " + cost + " (✧) і збільшив своє щоденне оновлення на " + increment + "!");
            editMessage(editMessage);
        } else {
            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(super.message.getChatId());
            editMessage.setMessageId(super.message.getMessageId());
            editMessage.setText("@" + reverenceUser.getUsername() + ", у вас недостатньо грошей (✧) у гаманці!\n\nНаявно на балансі: " + reverenceUser.getBalance() + " (✧)\n\nНеобхідно для цієї дії: " + cost + " (✧)");
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
