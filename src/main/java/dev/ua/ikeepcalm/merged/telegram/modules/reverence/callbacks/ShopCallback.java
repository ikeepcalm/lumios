package dev.ua.ikeepcalm.merged.telegram.modules.reverence.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class ShopCallback
        extends CommandParent {

    public void manage(String receivedCallback, CallbackQuery origin) {
        ShoppingUser whoCalled = this.shopService.find(origin.getFrom().getId(), this.chatService.find(origin.getMessage().getChatId()));
        if (whoCalled == null) {
            this.sendCallbackMessage(origin, "@" + origin.getFrom().getUserName() + ", спочатку виконайте /buy_increase@queueupnow_bot!");
            return;
        }
        try {
            ReverenceUser user = this.userService.findById(origin.getFrom().getId(), whoCalled.getChannel());
            switch (receivedCallback) {
                case "shop_10" -> {
                    this.shopService.delete(whoCalled);
                    if (user.getBalance() >= 100) {
                        user.setBalance(user.getBalance() - 100);
                        user.setSustainable(user.getSustainable() + 10);
                        this.userService.save(user);
                        sendCallbackMessage(origin, "@" + user.getUsername() + " витратив 100✧ і збільшив своє щоденне оновлення на 10!");
                        return;
                    }
                    sendCallbackMessage(origin, "@" + user.getUsername() + ", у вас недостатньо грошей у гаманці ✧!\nНаявно на балансі: " + user.getBalance() + "✧\nНеобхідно для цієї дії: 100✧");
                }
                case "shop_50" -> {
                    this.shopService.delete(whoCalled);
                    if (user.getBalance() >= 500) {
                        user.setBalance(user.getBalance() - 500);
                        user.setSustainable(user.getSustainable() + 50);
                        this.userService.save(user);
                        sendCallbackMessage(origin, "@" + user.getUsername() + " витратив 500✧ і збільшив своє щоденне оновлення на 50!");
                        return;
                    }
                    this.sendCallbackMessage(origin, "@" + user.getUsername() + ", у вас недостатньо грошей у гаманці ✧!\nНаявно на балансі: " + user.getBalance() + "✧\nНеобхідно для цієї дії: 500✧");
                }
                case "shop_100" -> {
                    this.shopService.delete(whoCalled);
                    if (user.getBalance() >= 1000) {
                        user.setBalance(user.getBalance() - 1000);
                        user.setSustainable(user.getSustainable() + 100);
                        this.userService.save(user);
                        sendCallbackMessage(origin, "@" + user.getUsername() + " витратив 1000✧ і збільшив своє щоденне оновлення на 100!");
                        return;
                    }
                    sendCallbackMessage(origin, "@" + user.getUsername() + ", у вас недостатньо грошей у гаманці ✧!\nНаявно на балансі: " + user.getBalance() + "✧\nНеобхідно для цієї дії: 1000✧");
                }
            }
        } finally {
            this.removeCallbackMessage(origin);
        }
    }
}

