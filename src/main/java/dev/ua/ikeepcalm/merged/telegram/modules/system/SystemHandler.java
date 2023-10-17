/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.modules.system;

import dev.ua.ikeepcalm.merged.telegram.modules.system.commands.HelpCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.ModuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SystemHandler implements ModuleHandler {


    private final HelpCommand helpCommand;

    @Autowired
    public SystemHandler(HelpCommand helpCommand){
        this.helpCommand = helpCommand;
    }

    public void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            if (command.equals("/help")) {
                helpCommand.execute(origin);
            }
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }

}

