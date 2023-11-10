package dev.ua.ikeepcalm.merged.telegram.modules.system;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.system.commands.HelpCommand;
import jakarta.ws.rs.core.Application;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SystemHandler implements HandlerParent {

    @Autowired
    private HelpCommand helpCommand;

    public void manageCommands(Update update) {
        Message message = update.getMessage();
        if (message != null) {
            String commandText = message.getText();
            if (commandText != null && commandText.startsWith("/")) {
                String[] parts = commandText.split("\\s+", 10);
                String command = parts[0];
                command = command.replace("@queueupnow_bot", "");
                if (command.equals("/help")) {
                    helpCommand.processUpdate(message);
                }
            }
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }
}

