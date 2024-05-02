package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system;

import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands.HelpCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands.InfoCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class SystemHandler implements HandlerParent {

    private final HelpCommand helpCommand;
    private final InfoCommand infoCommand;

    public SystemHandler(HelpCommand helpCommand, InfoCommand infoCommand) {
        this.helpCommand = helpCommand;
        this.infoCommand = infoCommand;
    }

    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        commandText = commandText.replace("@queueupnow_bot", "");
        switch (commandText) {
            case "/help" -> helpCommand.handleUpdate(message);
            case "/info" -> infoCommand.handleUpdate(message);
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
                if (update.getMessage().hasText() && !update.getMessage().getText().isEmpty()) {
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

