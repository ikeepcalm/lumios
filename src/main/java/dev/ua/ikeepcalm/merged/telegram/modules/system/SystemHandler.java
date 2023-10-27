package dev.ua.ikeepcalm.merged.telegram.modules.system;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.system.commands.HelpCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SystemHandler implements HandlerParent {

    private final HelpCommand helpCommand;

    public SystemHandler(HelpCommand helpCommand) {
        this.helpCommand = helpCommand;
    }

    public void manageCommands(Update update) {
        Message origin = update.getMessage();
        if (origin != null) {
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
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }
}

