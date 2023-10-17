/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.modules.tasks;

import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.TaskCreationCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.TaskEditingCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands.WhatIsDueCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.ModuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TasksHandler implements ModuleHandler {


    private final TaskCreationCommand taskCreationCommand;
    private final TaskEditingCommand taskEditingCommand;
    private final WhatIsDueCommand whatIsDueCommand;

    @Autowired
    public TasksHandler(TaskCreationCommand taskCreationCommand,
                        TaskEditingCommand taskEditingCommand,
                        WhatIsDueCommand whatIsDueCommand){

        this.taskCreationCommand = taskCreationCommand;
        this.taskEditingCommand = taskEditingCommand;
        this.whatIsDueCommand = whatIsDueCommand;
    }

    public void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command){
                case "/task" -> taskCreationCommand.execute(origin);
                case "/edit" -> taskEditingCommand.execute(origin);
                case "/whatisduetoday" -> whatIsDueCommand.execute(origin);
            }
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }

}

