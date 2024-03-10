package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.tasks.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.wrappers.TaskWrapper;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.TimetableEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class TaskParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule().addSerializer(LocalTime.class, new LocalTimeSerializer()));
    }

    public static List<DueTask> parseTaskMessage(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerForListOf(TaskWrapper.class);
        List<TaskWrapper> taskWrappers = objectReader.readValue(json);
        List<DueTask> tasks = new java.util.ArrayList<>();
        for (TaskWrapper taskWrapper : taskWrappers) {
            tasks.add(new DueTask(taskWrapper));
        } return tasks;
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

