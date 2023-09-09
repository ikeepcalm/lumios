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
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged.AllCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.charged.RaiseCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.reverence.AddCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.reverence.DecreaseCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.statistics.MeCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.statistics.StatsCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceJoinCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceSaveQueues;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Serverside.ForceUpdateCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside.RegisterCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside.StartCommand;
import dev.ua.ikeepcalm.merged.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler
implements Handleable {
    private QueueCommand queueCommand;
    private AllCommand allCommand;
    private RaiseCommand raiseCommand;
    private AddCommand addCommand;
    private DecreaseCommand decreaseCommand;
    private MeCommand meCommand;
    private StatsCommand statsCommand;
    private StartCommand startCommand;
    private ForceJoinCommand forceJoinCommand;
    private ForceUpdateCommand forceUpdateCommand;
    private ForceSaveQueues forceSaveQueues;
    private RegisterCommand registerCommand;

    @Autowired
    public CommandHandler(StartCommand startCommand, ForceSaveQueues forceSaveQueues, QueueCommand queueCommand, AllCommand allCommand, RaiseCommand raiseCommand, AddCommand addCommand, DecreaseCommand decreaseCommand, MeCommand meCommand, StatsCommand statsCommand, ForceJoinCommand forceJoinCommand, ForceUpdateCommand forceUpdateCommand, RegisterCommand registerCommand) {
        this.queueCommand = queueCommand;
        this.forceSaveQueues = forceSaveQueues;
        this.allCommand = allCommand;
        this.raiseCommand = raiseCommand;
        this.addCommand = addCommand;
        this.decreaseCommand = decreaseCommand;
        this.startCommand = startCommand;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
        this.forceJoinCommand = forceJoinCommand;
        this.forceUpdateCommand = forceUpdateCommand;
        this.registerCommand = registerCommand;
    }

    @Override
    public void manage(Update update) {
        Message origin = update.getMessage();
        if (origin.getText() != null) {
            if (origin.getText().startsWith("/queue")) {
                this.queueCommand.execute(origin);
            } else if (origin.getText().startsWith("/start")) {
                this.startCommand.execute(origin);
            } else if (origin.getText().startsWith("/add")) {
                this.addCommand.execute(origin);
            } else if (origin.getText().startsWith("/increase")) {
                this.raiseCommand.execute(origin);
            } else if (origin.getText().startsWith("/decrease")) {
                this.decreaseCommand.execute(origin);
            } else if (origin.getText().startsWith("/me")) {
                this.meCommand.execute(origin);
            } else if (origin.getText().startsWith("/stats")) {
                this.statsCommand.execute(origin);
            } else if (origin.getText().startsWith("/forcejoin")) {
                this.forceJoinCommand.execute(origin);
            } else if (origin.getText().startsWith("/forceupdate")) {
                this.forceUpdateCommand.execute(origin);
            } else if (origin.getText().startsWith("/forcesave")) {
                this.forceSaveQueues.execute(origin);
            } else if (origin.getText().startsWith("/register")) {
                this.registerCommand.execute(origin);
            } else if (origin.getText().startsWith("@all") || origin.getText().startsWith("/all")) {
                this.allCommand.execute(origin);
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

