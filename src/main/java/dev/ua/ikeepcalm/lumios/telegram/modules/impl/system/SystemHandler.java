package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system;

import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.callbacks.TimetableCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands.HelpCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands.InfoCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands.SettingsCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class SystemHandler implements HandlerParent {

    private final HelpCommand helpCommand;
    private final InfoCommand infoCommand;
    private final SettingsCommand settingsCommand;
    private final TimetableCallback timetableCallback;

    public SystemHandler(HelpCommand helpCommand, InfoCommand infoCommand, SettingsCommand settingsCommand, TimetableCallback timetableCallback) {
        this.helpCommand = helpCommand;
        this.infoCommand = infoCommand;
        this.settingsCommand = settingsCommand;
        this.timetableCallback = timetableCallback;
    }

    public void manageCommands(Update update) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.startsWith("settings-timetable-")) {
                timetableCallback.handleUpdate(update.getCallbackQuery());
            }
        } else {
            Message message = update.getMessage();
            String commandText = message.getText();
            commandText = commandText.replace("@lumios_bot", "");
            switch (commandText) {
                case "/help", "/start help" -> helpCommand.handleUpdate(message);
                case "/info" -> infoCommand.handleUpdate(message);
                case "/settings" -> settingsCommand.handleUpdate(message);
            }
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
                return update.hasCallbackQuery();
            }
        } else {
            return false;
        }
    }
}

