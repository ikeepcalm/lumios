package dev.ua.ikeepcalm.queue.telegram.modules.reverence;

import dev.ua.ikeepcalm.queue.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queue.telegram.modules.reverence.callbacks.ShopCallback;
import dev.ua.ikeepcalm.queue.telegram.modules.reverence.commands.*;
import dev.ua.ikeepcalm.queue.telegram.modules.reverence.patterns.ReverencePatterns;
import dev.ua.ikeepcalm.queue.telegram.modules.reverence.updates.DecreasingUpdate;
import dev.ua.ikeepcalm.queue.telegram.modules.reverence.updates.IncreasingUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ReverenceHandler implements HandlerParent {

    private final ShopCommand shopCommand;
    private final ShopCallback shopCallback;
    private final IncreaseCommand increaseCommand;
    private final DecreaseCommand decreaseCommand;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;
    private final DecreasingUpdate decreasingUpdate;
    private final IncreasingUpdate increasingUpdate;

    @Autowired
    public ReverenceHandler(ShopCommand shopCommand,
                            ShopCallback shopCallback,
                            IncreaseCommand increaseCommand,
                            DecreaseCommand decreaseCommand,
                            MeCommand meCommand,
                            StatsCommand statsCommand,
                            DecreasingUpdate decreasingUpdate,
                            IncreasingUpdate increasingUpdate) {
        this.shopCommand = shopCommand;
        this.shopCallback = shopCallback;
        this.increaseCommand = increaseCommand;
        this.decreaseCommand = decreaseCommand;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.decreasingUpdate = decreasingUpdate;
        this.increasingUpdate = increasingUpdate;
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            manageCallbacks(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            manageCommands(update.getMessage());
        } else {
            manageUpdates(update);
        }
    }

    private void manageCommands(org.telegram.telegrambots.meta.api.objects.Message message) {
        String commandText = message.getText();
        String[] parts = commandText.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        command = command.replace("@queueupnow_bot", "");
        switch (command) {
            case "/shop" -> shopCommand.processUpdate(message);
            case "/increase" -> increaseCommand.processUpdate(message);
            case "/decrease" -> decreaseCommand.processUpdate(message);
            case "/me" -> meCommand.processUpdate(message);
            case "/stats" -> statsCommand.processUpdate(message);
        }
    }

    private void manageUpdates(Update update) {
        if (ReverencePatterns.isIncreasingUpdate(update.getMessage())) {
            increasingUpdate.processUpdate(update.getMessage());
        } else if (ReverencePatterns.isDecreasingUpdate(update.getMessage())) {
            decreasingUpdate.processUpdate(update.getMessage());
        }
    }

    private void manageCallbacks(CallbackQuery callbackQuery) {
        String callback = callbackQuery.getData();
        if (callback.startsWith("shop_")) {
            shopCallback.processUpdate(callbackQuery);
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().isCommand() || update.getMessage().isReply();
        } else {
            return update.hasCallbackQuery();
        }
    }
}
