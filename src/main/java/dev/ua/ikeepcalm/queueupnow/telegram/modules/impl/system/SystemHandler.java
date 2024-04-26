package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.system;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.system.commands.AuthCommand;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.system.commands.HelpCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SystemHandler implements HandlerParent {

    private final HelpCommand helpCommand;
    private final AuthCommand authCommand;

    public SystemHandler(HelpCommand helpCommand, AuthCommand authCommand) {
        this.helpCommand = helpCommand;
        this.authCommand = authCommand;
    }

    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        commandText = commandText.replace("@queueupnow_bot", "");
        if (commandText.equals("/help")) {
            helpCommand.handleUpdate(message);
        } else if (commandText.equals("/auth")) {
            authCommand.handleUpdate(message);
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        manageCommands(update);
    }

    @Override
    public boolean supports(Update update) {
        if (update != null) {
            if (update.getMessage() != null) {
                if (update.getMessage().hasText() && ! update.getMessage().getText().isEmpty()) {
                    return update.getMessage().getText().startsWith("/");
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}

