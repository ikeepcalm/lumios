package dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable;


import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.commands.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class TimetableHandler implements HandlerParent {

    private final EditorCommand editorCommand;
    private final TodayCommand todayCommand;
    private final WeekCommand weekCommand;
    private final NowCommand nowCommand;
    private final NextCommand nextCommand;
    private final TomorrowCommand tomorrowCommand;

    public TimetableHandler(EditorCommand editorCommand,
                            TodayCommand todayCommand,
                            WeekCommand weekCommand,
                            NowCommand nowCommand,
                            NextCommand nextCommand,
                            TomorrowCommand tomorrowCommand) {
        this.editorCommand = editorCommand;
        this.todayCommand = todayCommand;
        this.weekCommand = weekCommand;
        this.nowCommand = nowCommand;
        this.nextCommand = nextCommand;
        this.tomorrowCommand = tomorrowCommand;
    }


    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        commandText = commandText.replace("@lumios_bot", "");
        switch (commandText) {
            case "/editor" -> editorCommand.handleUpdate(message);
            case "/today" -> todayCommand.handleUpdate(message);
            case "/week" -> weekCommand.handleUpdate(message);
            case "/now" -> nowCommand.handleUpdate(message);
            case "/next" -> nextCommand.handleUpdate(message);
            case "/tomorrow" -> tomorrowCommand.handleUpdate(message);
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

