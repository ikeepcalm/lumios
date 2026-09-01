package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.utils.SlotViewRenderer;
import org.springframework.stereotype.Component;

@Component
@BotCommand(command = "now", aliases = {"meow"})
public class NowCommand extends SlotCommand {

    public NowCommand(SlotViewRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String commandType() {
        return "now";
    }
}
