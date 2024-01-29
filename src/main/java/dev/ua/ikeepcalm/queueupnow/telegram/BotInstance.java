package dev.ua.ikeepcalm.queueupnow.telegram;

import dev.ua.ikeepcalm.queueupnow.telegram.config.TelegramBotConfig;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.List;

@Component
public class BotInstance extends TelegramLongPollingBot implements LongPollingBot {

    public final String botUsername;
    private final List<HandlerParent> handlerParentList;

    public void onUpdateReceived(Update update) {
        for (HandlerParent handlerParent : this.handlerParentList) {
            if (handlerParent.supports(update)){
                handlerParent.dispatchUpdate(update);
            }
        }
    }

    public BotInstance(TelegramBotConfig config, List<HandlerParent> handlerParentList) {
        super(config.getToken());
        getOptions().setAllowedUpdates(List.of("message", "callback_query", "message_reaction", "chat_member"));
        this.handlerParentList = handlerParentList;
        this.botUsername = config.getUsername();
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }
}

