package dev.ua.ikeepcalm.queueupnow.telegram;

import dev.ua.ikeepcalm.queueupnow.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@PropertySource("classpath:thirdparty.properties")
public class BotInstance extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    public static String botUsername;

    private final List<Handleable> handleableList;

    @Override
    public void onUpdateReceived(Update update) {
        for (Handleable h : handleableList) {
            if (h.supports(update)) {
                h.manage(update);
            }
        }
    }

    public BotInstance(@Value("${telegram.bot.token}") String botToken, List<Handleable> handleableList) {
        super(botToken);
        this.handleableList = handleableList;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
