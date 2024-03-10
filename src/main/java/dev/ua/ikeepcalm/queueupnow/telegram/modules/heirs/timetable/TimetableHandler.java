package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.timetable;


import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.timetable.commands.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TimetableHandler implements HandlerParent {

    private final FeedCommand feedCommand;
    private final TodayCommand todayCommand;
    private final WeekCommand weekCommand;
    private final NowCommand nowCommand;
    private final NextCommand nextCommand;
    private final TomorrowCommand tomorrowCommand;

    public TimetableHandler(FeedCommand feedCommand,
                            TodayCommand todayCommand,
                            WeekCommand weekCommand,
                            NowCommand nowCommand,
                            NextCommand nextCommand,
                            TomorrowCommand tomorrowCommand) {
        this.feedCommand = feedCommand;
        this.todayCommand = todayCommand;
        this.weekCommand = weekCommand;
        this.nowCommand = nowCommand;
        this.nextCommand = nextCommand;
        this.tomorrowCommand = tomorrowCommand;
    }


    public void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        commandText = commandText.replace("@queueupnow_bot", "");
        switch (commandText) {
            case "/feed" -> feedCommand.processUpdate(message);
            case "/today" -> todayCommand.processUpdate(message);
            case "/week" -> weekCommand.processUpdate(message);
            case "/now" -> nowCommand.processUpdate(message);
            case "/next" -> nextCommand.processUpdate(message);
            case "/tomorrow" -> tomorrowCommand.processUpdate(message);
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
