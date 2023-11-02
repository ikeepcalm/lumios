package dev.ua.ikeepcalm.merged.telegram.modules.reverence;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.callbacks.ShopCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.*;

import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.ReverencePatterns;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.DecreasingUpdate;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.IncreasingUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ReverenceHandler implements HandlerParent {

    private final StartCommand startCommand;
    private final ShopCommand shopCommand;
    private final ShopCallback shopCallback;
    private final IncreaseCommand increaseCommand;
    private final DecreaseCommand decreaseCommand;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;
    private final RegisterCommand registerCommand;
    private final DecreasingUpdate decreasingUpdate;
    private final IncreasingUpdate increasingUpdate;

    public ReverenceHandler(StartCommand startCommand,
                            ShopCommand shopCommand,
                            ShopCallback shopCallback,
                            IncreaseCommand increaseCommand,
                            DecreaseCommand decreaseCommand,
                            MeCommand meCommand,
                            StatsCommand statsCommand,
                            RegisterCommand registerCommand,
                            DecreasingUpdate decreasingUpdate,
                            IncreasingUpdate increasingUpdate) {
        this.startCommand = startCommand;
        this.shopCommand = shopCommand;
        this.shopCallback = shopCallback;
        this.increaseCommand = increaseCommand;
        this.decreaseCommand = decreaseCommand;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.registerCommand = registerCommand;
        this.decreasingUpdate = decreasingUpdate;
        this.increasingUpdate = increasingUpdate;
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            manageCallbacks(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            manageCommands(update.getMessage());
        } else {
            manageUpdates(update);
        }
    }

    private void manageCommands(org.telegram.telegrambots.meta.api.objects.Message message) {
        String commandText = message.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            command = command.replace("@queueupnow_bot", "");
            switch (command) {
                case "/start" -> startCommand.execute(message);
                case "/shop" -> shopCommand.execute(message);
                case "/increase" -> increaseCommand.execute(message);
                case "/decrease" -> decreaseCommand.execute(message);
                case "/me" -> meCommand.execute(message);
                case "/stats" -> statsCommand.execute(message);
                case "/register" -> registerCommand.execute(message);
            }
        }
    }

    private void manageUpdates(Update update) {
        if (ReverencePatterns.isIncreasingUpdate(update)) {
            increasingUpdate.execute(update);
        } else if (ReverencePatterns.isDecreasingUpdate(update)) {
            decreasingUpdate.execute(update);
        }
    }

    private void manageCallbacks(CallbackQuery callbackQuery) {
        String callback = callbackQuery.getData();
        if (callback.startsWith("shop_")) {
            shopCallback.manage(callback, callbackQuery);
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
