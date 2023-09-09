/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.PropertySource
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.bots.TelegramLongPollingBot
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged;

import dev.ua.ikeepcalm.merged.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@PropertySource(value={"classpath:thirdparty.properties"})
public class BotInstance
extends TelegramLongPollingBot {
    @Value(value="${telegram.bot.username}")
    public static String botUsername;
    private final List<Handleable> handleableList;

    public void onUpdateReceived(Update update) {
        for (Handleable h : this.handleableList) {
            if (!h.supports(update)) continue;
            h.manage(update);
        }
    }

    public BotInstance(@Value(value="${telegram.bot.token}") String botToken, List<Handleable> handleableList) {
        super("6633653487:AAHarE9iDC7VIhQDGhNGiZXK2tuanYgg7oY");
        this.handleableList = handleableList;
    }

    public String getBotUsername() {
        return "queueupnow_bot";
    }
}

