package dev.ua.ikeepcalm.merged.telegram.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.merged.database.entities.timetable.Timetable;

import java.io.IOException;

public class TimetableParser {

    public static Timetable parseTimetableMessage(String jsonMessage) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        return objectMapper.readValue(jsonMessage, Timetable.class);
    }

}
