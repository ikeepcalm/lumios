package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.utils.SlotViewRenderer;
import org.springframework.stereotype.Component;

@Component
@BotCommand(command = "next")
public class NextCommand extends SlotCommand {

    public NextCommand(SlotViewRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String commandType() {
        return "next";
    }
}
