package dev.ua.ikeepcalm.merged.telegram;

import dev.ua.ikeepcalm.merged.telegram.config.TelegramBotConfig;
import dev.ua.ikeepcalm.merged.telegram.modules.ModuleHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.List;

@Component
public class BotInstance  extends TelegramLongPollingBot implements LongPollingBot {

    public final String botUsername;
    private final List<ModuleHandler> moduleHandlerList;

    public void onUpdateReceived(Update update) {
        for (ModuleHandler moduleHandler : this.moduleHandlerList) {
            if (moduleHandler.supports(update)){
                moduleHandler.dispatchUpdate(update);
            };
        }
    }

                public BotInstance(TelegramBotConfig config, List<ModuleHandler> moduleHandlerList) {
        super(config.getToken());
        this.moduleHandlerList = moduleHandlerList;
        this.botUsername = config.getUsername();
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }
}

