package dev.ua.ikeepcalm.merged.telegram.modules.timetable.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.merged.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.merged.database.entities.timetable.wrappers.TimetableWrapper;

import java.io.IOException;
import java.util.List;

public class TimetableParser {

    public static TimetableEntry parseTimetableMessage(String jsonMessage) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new TimetableEntry(objectMapper.readValue(jsonMessage, TimetableWrapper.class));
    }

    public static String parseTimetableObjects(List<TimetableWrapper> timetableEntries){
        ObjectWriter objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        try {
            return objectMapper.writeValueAsString(timetableEntries);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parseClassEmoji(ClassType classType){
        return switch (classType.name()){
            case "LECTURE" -> "\uD83D\uDD35";
            case "PRACTICE" -> "\uD83D\uDFE0";
            case "LAB" -> "\uD83D\uDFE2";
            default -> "?";
        };
    }

}
