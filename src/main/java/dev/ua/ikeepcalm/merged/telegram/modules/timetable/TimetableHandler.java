package dev.ua.ikeepcalm.merged.telegram.modules.timetable;

import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands.TodayCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands.FeedCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands.WeekCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TimetableHandler implements HandlerParent {

    private final FeedCommand feedCommand;
    private final TodayCommand todayCommand;
    private final WeekCommand weekCommand;

    public TimetableHandler(FeedCommand feedCommand, TodayCommand todayCommand, WeekCommand weekCommand) {
        this.feedCommand = feedCommand;
        this.todayCommand = todayCommand;
        this.weekCommand = weekCommand;
    }


    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command) {
                case "/feed" -> feedCommand.processUpdate(message);
                case "/today" -> todayCommand.processUpdate(message);
                case "/week" -> weekCommand.processUpdate(message);
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

