package dev.ua.ikeepcalm.merged.telegram.modules.timetable;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands.WeekCreationCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TimetableHandler implements HandlerParent {

    private final WeekCreationCommand weekCreationCommand;

    public TimetableHandler(WeekCreationCommand weekCreationCommand) {
        this.weekCreationCommand = weekCreationCommand;
    }

    public void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command) {
                case "/week" -> weekCreationCommand.execute(origin);
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

