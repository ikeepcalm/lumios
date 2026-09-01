package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.utils.DayViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@BotCommand(command = "tomorrow")
public class TomorrowCommand extends DayCommand {

    public TomorrowCommand(DayViewRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String commandType() {
        return "tomorrow";
    }

    /**
     * The date, not just the day name: tomorrow can fall in the other half of the two-week cycle, which
     * is what previously made Sunday show the wrong Monday.
     */
    @Override
    protected LocalDate date() {
        return TimetableClock.today().plusDays(1);
    }
}
