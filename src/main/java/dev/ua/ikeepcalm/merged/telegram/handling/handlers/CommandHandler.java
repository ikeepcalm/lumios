/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.handling.handlers;

import dev.ua.ikeepcalm.merged.telegram.executing.commands.queues.QueueCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged.EveryoneCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged.ShopCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.reverence.IncreaseCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.reverence.DecreaseCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.statistics.MeCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.statistics.StatsCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceJoinCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceSaveQueues;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceUpdateCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside.HelpCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside.RegisterCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside.StartCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.tasks.TaskCreationCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.tasks.TaskEditingCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.tasks.WhatIsDueCommand;
import dev.ua.ikeepcalm.merged.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler
implements Handleable {
    private final QueueCommand queueCommand;
    private final EveryoneCommand everyoneCommand;
    private final ShopCommand shopCommand;
    private final IncreaseCommand increaseCommand;
    private final DecreaseCommand decreaseCommand;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;
    private final StartCommand startCommand;
    private final TaskCreationCommand taskCreationCommand;
    private final WhatIsDueCommand whatIsDueCommand;
    private final ForceJoinCommand forceJoinCommand;
    private final ForceUpdateCommand forceUpdateCommand;
    private final ForceSaveQueues forceSaveQueues;
    private final RegisterCommand registerCommand;
    private final TaskEditingCommand taskEditingCommand;
    private final HelpCommand helpCommand;

    @Autowired
    public CommandHandler(StartCommand startCommand, ForceSaveQueues forceSaveQueues, QueueCommand queueCommand, EveryoneCommand everyoneCommand, ShopCommand shopCommand, IncreaseCommand increaseCommand, DecreaseCommand decreaseCommand, MeCommand meCommand, StatsCommand statsCommand, ForceJoinCommand forceJoinCommand, ForceUpdateCommand forceUpdateCommand, RegisterCommand registerCommand, TaskCreationCommand taskCreationCommand, WhatIsDueCommand whatIsDueCommand, TaskEditingCommand taskEditingCommand, HelpCommand helpCommand) {
        this.queueCommand = queueCommand;
        this.forceSaveQueues = forceSaveQueues;
        this.everyoneCommand = everyoneCommand;
        this.shopCommand = shopCommand;
        this.increaseCommand = increaseCommand;
        this.decreaseCommand = decreaseCommand;
        this.startCommand = startCommand;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.forceJoinCommand = forceJoinCommand;
        this.forceUpdateCommand = forceUpdateCommand;
        this.registerCommand = registerCommand;
        this.taskCreationCommand = taskCreationCommand;
        this.whatIsDueCommand = whatIsDueCommand;
        this.taskEditingCommand = taskEditingCommand;
        this.helpCommand = helpCommand;
    }

    @Override
    public void manage(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            commandText = commandText.replace("@queueupnow_bot", "");
            switch (commandText) {
                case "/queue" -> this.queueCommand.execute(origin);
                case "/start" -> this.startCommand.execute(origin);
                case "/shop" -> this.shopCommand.execute(origin);
                case "/increase" -> this.increaseCommand.execute(origin);
                case "/decrease" -> this.decreaseCommand.execute(origin);
                case "/me" -> this.meCommand.execute(origin);
                case "/stats" -> this.statsCommand.execute(origin);
                case "/forcejoin" -> this.forceJoinCommand.execute(origin);
                case "/forceupdate" -> this.forceUpdateCommand.execute(origin);
                case "/forcesave" -> this.forceSaveQueues.execute(origin);
                case "/register" -> this.registerCommand.execute(origin);
                case "/everyone" -> this.everyoneCommand.execute(origin);
                case "/task" -> this.taskCreationCommand.execute(origin);
                case "/whatisduetoday" -> this.whatIsDueCommand.execute(origin);
                case "/edit" -> this.taskEditingCommand.execute(origin);
                case "/help" -> this.helpCommand.execute(origin);
            }
        }

    }

    @Override
    public boolean supports(Update update) {
        if (update != null) {
            if (update.getMessage() != null) {
                if (update.getMessage().getText() != null) {
                    return update.getMessage().getText().startsWith("/");
                }
                return false;
            }
            return false;
        }
        return false;
    }
}

