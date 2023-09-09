/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 */
package dev.ua.ikeepcalm.merged.telegram.executing.callbacks;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.entities.reverence.ShoppingUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class IncreaseCallback
extends Executable {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void manage(String receivedCallback, CallbackQuery origin) {
        ShoppingUser whoCalled = this.raiseService.find(origin.getFrom().getId(), this.chatService.find(origin.getMessage().getChatId()));
        if (whoCalled == null) {
            this.sendCallbackMessage(origin, "@" + origin.getFrom().getUserName() + ", \u0441\u043f\u043e\u0447\u0430\u0442\u043a\u0443 \u0432\u0438\u043a\u043e\u043d\u0430\u0439\u0442\u0435 /buy_increase@queueupnow_bot!");
            return;
        }
        try {
            ReverenceUser user = this.userService.findById(origin.getFrom().getId(), whoCalled.getChannel());
            switch (receivedCallback) {
                case "increase_10": {
                    this.raiseService.delete(whoCalled);
                    if (user.getBalance() >= 100) {
                        user.setBalance(user.getBalance() - 100);
                        user.setSustainable(user.getSustainable() + 10);
                        this.userService.save(user);
                        this.sendCallbackMessage(origin, "@" + user.getUsername() + " \u0432\u0438\u0442\u0440\u0430\u0442\u0438\u0432 100\u2727 \u0456 \u0437\u0431\u0456\u043b\u044c\u0448\u0438\u0432 \u0441\u0432\u043e\u0454 \u0449\u043e\u0434\u0435\u043d\u043d\u0435 \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f \u043d\u0430 10!");
                        return;
                    }
                    this.sendCallbackMessage(origin, "@" + user.getUsername() + ", \u0443 \u0432\u0430\u0441 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u0433\u0440\u043e\u0448\u0435\u0439 \u0443 \u0433\u0430\u043c\u0430\u043d\u0446\u0456 \u2727!\n\u041d\u0430\u044f\u0432\u043d\u043e \u043d\u0430 \u0431\u0430\u043b\u0430\u043d\u0441\u0456: " + user.getBalance() + "\u2727\n\u041d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e \u0434\u043b\u044f \u0446\u0456\u0454\u0457 \u0434\u0456\u0457: 100\u2727");
                    return;
                }
                case "increase_50": {
                    this.raiseService.delete(whoCalled);
                    if (user.getBalance() >= 500) {
                        user.setBalance(user.getBalance() - 500);
                        user.setSustainable(user.getSustainable() + 50);
                        this.userService.save(user);
                        this.sendCallbackMessage(origin, "@" + user.getUsername() + " \u0432\u0438\u0442\u0440\u0430\u0442\u0438\u0432 500\u2727 \u0456 \u0437\u0431\u0456\u043b\u044c\u0448\u0438\u0432 \u0441\u0432\u043e\u0454 \u0449\u043e\u0434\u0435\u043d\u043d\u0435 \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f \u043d\u0430 50!");
                        return;
                    }
                    this.sendCallbackMessage(origin, "@" + user.getUsername() + ", \u0443 \u0432\u0430\u0441 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u0433\u0440\u043e\u0448\u0435\u0439 \u0443 \u0433\u0430\u043c\u0430\u043d\u0446\u0456 \u2727!\n\u041d\u0430\u044f\u0432\u043d\u043e \u043d\u0430 \u0431\u0430\u043b\u0430\u043d\u0441\u0456: " + user.getBalance() + "\u2727\n\u041d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e \u0434\u043b\u044f \u0446\u0456\u0454\u0457 \u0434\u0456\u0457: 500\u2727");
                    return;
                }
                case "increase_100": {
                    this.raiseService.delete(whoCalled);
                    if (user.getBalance() >= 1000) {
                        user.setBalance(user.getBalance() - 1000);
                        user.setSustainable(user.getSustainable() + 100);
                        this.userService.save(user);
                        this.sendCallbackMessage(origin, "@" + user.getUsername() + " \u0432\u0438\u0442\u0440\u0430\u0442\u0438\u0432 1000\u2727 \u0456 \u0437\u0431\u0456\u043b\u044c\u0448\u0438\u0432 \u0441\u0432\u043e\u0454 \u0449\u043e\u0434\u0435\u043d\u043d\u0435 \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f \u043d\u0430 100!");
                        return;
                    }
                    this.sendCallbackMessage(origin, "@" + user.getUsername() + ", \u0443 \u0432\u0430\u0441 \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u0433\u0440\u043e\u0448\u0435\u0439 \u0443 \u0433\u0430\u043c\u0430\u043d\u0446\u0456 \u2727!\n\u041d\u0430\u044f\u0432\u043d\u043e \u043d\u0430 \u0431\u0430\u043b\u0430\u043d\u0441\u0456: " + user.getBalance() + "\u2727\n\u041d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e \u0434\u043b\u044f \u0446\u0456\u0454\u0457 \u0434\u0456\u0457: 1000\u2727");
                    return;
                }
            }
            return;
        }
        finally {
            this.deleteCallbackMessage(origin);
        }
    }
}

