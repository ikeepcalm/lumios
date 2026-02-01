package dev.ua.ikeepcalm.lumios.telegram.utils.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers.TimetableWrapper;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    public static final String EMOJI_LEGEND = "``` 🔵 - ЛЕКЦІЯ\n 🟠 - ПРАКТИКА\n 🟢 - ЛАБОРАТОРНА```\n\n";

    public static String parseClassEmoji(ClassType classType) {
        return switch (classType.name()) {
            case "LECTURE" -> "\uD83D\uDD35";
            case "PRACTICE" -> "\uD83D\uDFE0";
            case "LAB" -> "\uD83D\uDFE2";
            default -> "?";
        };
    }

    public static String formatClassEntriesGroupedByTime(List<ClassEntry> classEntries) {
        if (classEntries == null || classEntries.isEmpty()) {
            return "";
        }

        Map<String, List<ClassEntry>> groupedByTime = new LinkedHashMap<>();
        for (ClassEntry entry : classEntries) {
            String timeSlot = entry.getStartTime() + " - " + entry.getEndTime();
            groupedByTime.computeIfAbsent(timeSlot, k -> new ArrayList<>()).add(entry);
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, List<ClassEntry>> group : groupedByTime.entrySet()) {
            result.append("*").append(group.getKey()).append("*\n");
            for (ClassEntry entry : group.getValue()) {
                result.append(parseClassEmoji(entry.getClassType()))
                        .append(" [").append(entry.getName()).append("](")
                        .append(entry.getUrl()).append(")\n");
            }
            result.append("\n");
        }

        return result.toString();
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

