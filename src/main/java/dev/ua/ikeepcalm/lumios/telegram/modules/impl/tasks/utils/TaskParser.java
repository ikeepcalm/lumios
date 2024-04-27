package dev.ua.ikeepcalm.lumios.telegram.modules.impl.tasks.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.wrappers.TaskWrapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TaskParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule().addSerializer(LocalTime.class, new LocalTimeSerializer()));
    }

    public static DueTask parseTaskMessage(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerFor(TaskWrapper.class);
        TaskWrapper taskWrappers = objectReader.readValue(json);
        return new DueTask(taskWrappers);
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

