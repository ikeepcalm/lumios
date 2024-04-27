package dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers.TimetableWrapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimetableParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()
                .addSerializer(LocalTime.class, new LocalTimeSerializer()));
    }

    public static List<TimetableEntry> parseTimetableMessage(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerForListOf(TimetableWrapper.class);
        List<TimetableWrapper> timetableWrappers = objectReader.readValue(json);
        List<TimetableEntry> timetableEntries = new ArrayList<>();

        for (TimetableWrapper timetableWrapper : timetableWrappers) {
            TimetableEntry timetableEntry = new TimetableEntry(timetableWrapper);
            timetableEntries.add(timetableEntry);
        }

        return timetableEntries;

    }


    public static String parseClassEmoji(ClassType classType) {
        return switch (classType.name()) {
            case "LECTURE" -> "\uD83D\uDD35";
            case "PRACTICE" -> "\uD83D\uDFE0";
            case "LAB" -> "\uD83D\uDFE2";
            default -> "?";
        };
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

