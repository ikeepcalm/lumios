package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class QueueParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule().addSerializer(LocalTime.class, new LocalTimeSerializer()));
    }

    public static QueueWrapper parseQueueMessage(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerFor(QueueWrapper.class);
        return objectReader.readValue(json);
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

