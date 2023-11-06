package dev.ua.ikeepcalm.merged.telegram.modules.timetable.commands;

import dev.ua.ikeepcalm.merged.database.entities.timetable.Timetable;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.utils.TimetableParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;

@Component
public class WeekCreationCommand extends CommandParent {


    public void execute(Message origin) {
        String message = origin.getReplyToMessage().getText();
        Timetable timeTable;
        try {
            timeTable = TimetableParser.parseTimetableMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } // Other things to implement a bit later
    }
}

