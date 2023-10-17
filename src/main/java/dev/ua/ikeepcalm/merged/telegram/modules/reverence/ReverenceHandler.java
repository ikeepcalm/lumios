/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.modules.reverence;

import dev.ua.ikeepcalm.merged.telegram.modules.reverence.callbacks.ShopCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.EveryoneCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.ShopCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.DecreaseCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.IncreaseCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.MeCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.StatsCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.RegisterCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands.StartCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.ModuleHandler;

import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.updates.DecreasingUpdate;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.updates.IncreasingUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ReverenceHandler implements ModuleHandler {

    private final StartCommand startCommand;
    private final ShopCommand shopCommand;
    private final ShopCallback shopCallback;
    private final IncreaseCommand increaseCommand;
    private final DecreaseCommand decreaseCommand;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;
    private final RegisterCommand registerCommand;
    private final EveryoneCommand everyoneCommand;
    private final dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.DecreasingUpdate decreasingUpdate;
    private final dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.IncreasingUpdate increasingUpdate;

    @Autowired
    public ReverenceHandler(StartCommand startCommand,
                            ShopCommand shopCommand,
                            ShopCallback shopCallback, IncreaseCommand increaseCommand,
                            DecreaseCommand decreaseCommand,
                            MeCommand meCommand,
                            StatsCommand statsCommand,
                            RegisterCommand registerCommand,
                            EveryoneCommand everyoneCommand,
                            dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.DecreasingUpdate decreasingUpdate,
                            dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates.IncreasingUpdate increasingUpdate){
        this.startCommand = startCommand;
        this.shopCommand = shopCommand;
        this.shopCallback = shopCallback;
        this.increaseCommand = increaseCommand;

        this.decreaseCommand = decreaseCommand;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.registerCommand = registerCommand;
        this.everyoneCommand = everyoneCommand;
        this.decreasingUpdate = decreasingUpdate;
        this.increasingUpdate = increasingUpdate;
    }

    private void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command){
                case "/start" -> startCommand.execute(origin);
                case "/shop" -> shopCommand.execute(origin);
                case "/increase" -> increaseCommand.execute(origin);
                case "/decrease" -> decreaseCommand.execute(origin);
                case "/me" -> meCommand.execute(origin);
                case "/stats" -> statsCommand.execute(origin);
                case "/register" -> registerCommand.execute(origin);
                case "/everyone" -> everyoneCommand.execute(origin);
            }
        }
    }

    private void manageUpdates(Update update){
        if (IncreasingUpdate.isIncreasingUpdate(update)){
            increasingUpdate.execute(update);
        } else if (DecreasingUpdate.isDecreasingUpdate(update)){
            decreasingUpdate.execute(update);
        }
    }

    private void manageCallbacks(Update update) {
        String callback = update.getCallbackQuery().getData();
        CallbackQuery origin = update.getCallbackQuery();
        if (callback.startsWith("increase_")) {
            shopCallback.manage(callback, origin);
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.hasCallbackQuery()){
            manageCallbacks(update);
        } else if (update.hasMessage() && update.getMessage().getText().startsWith("/")){
            manageCommands(update);
        } else {
            manageUpdates(update);
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()){
                if (update.getMessage().isReply() && (IncreasingUpdate.isIncreasingUpdate(update) || DecreasingUpdate.isDecreasingUpdate(update))){
                    return true;
                } else {
                    return update.getMessage().getText().startsWith("/");
                }
            } else {
                return false;
            }
        } else {
            return update.hasCallbackQuery();
        }
    }
}

