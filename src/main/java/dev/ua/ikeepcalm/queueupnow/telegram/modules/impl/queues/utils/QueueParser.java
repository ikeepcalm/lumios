package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.wrappers.QueueWrapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QueueParser {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule().addSerializer(LocalTime.class, new LocalTimeSerializer()));
    }

    public static List<SimpleQueue> parseQueueMessage(String json) throws JsonProcessingException {
        ObjectReader objectReader = objectMapper.readerForListOf(QueueWrapper.class);
        List<QueueWrapper> queueWrappers = objectReader.readValue(json);
        List<SimpleQueue> queueList = new java.util.ArrayList<>();
        for (QueueWrapper taskWrapper : queueWrappers) {
            queueList.add(new SimpleQueue(taskWrapper));
        } return queueList;
    }

    private static class LocalTimeSerializer extends com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer {
        public LocalTimeSerializer() {
            super(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

}

