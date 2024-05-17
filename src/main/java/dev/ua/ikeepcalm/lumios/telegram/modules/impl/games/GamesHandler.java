package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games;

import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.commands.SoloGambleCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class GamesHandler implements HandlerParent {

    private final SoloGambleCommand soloGambleCommand;

    public GamesHandler(SoloGambleCommand soloGambleCommand) {
        this.soloGambleCommand = soloGambleCommand;
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.getMessage().getChat().getType().equals("private")) {
            return;
        }
        String commandText = update.getMessage().getText();
        String[] parts = commandText.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        command = command.replace("@lumios_bot", "");
        switch (command) {
            case "/gamble" -> soloGambleCommand.handleUpdate(update.getMessage());
        }
    }


    @Override
    public boolean supports(Update update) {
        if (update != null) {
            if (update.hasMessage() && update.getMessage() != null) {
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