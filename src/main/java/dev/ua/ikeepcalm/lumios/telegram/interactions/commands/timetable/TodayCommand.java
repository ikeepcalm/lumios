package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.utils.DayViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@BotCommand(command = "today")
public class TodayCommand extends DayCommand {

    public TodayCommand(DayViewRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String commandType() {
        return "today";
    }

    @Override
    protected LocalDate date() {
        return TimetableClock.today();
    }
}
