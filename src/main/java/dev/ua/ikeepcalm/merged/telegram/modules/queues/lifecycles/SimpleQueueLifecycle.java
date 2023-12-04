package dev.ua.ikeepcalm.merged.telegram.modules.queues.lifecycles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ua.ikeepcalm.merged.database.entities.queue.SimpleQueue;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Component
public class SimpleQueueLifecycle {
    private final ObjectMapper objectMapper;
    private static final String SIMPLE_QUEUES_FILE_NAME = "simpleQueues.json";

    public SimpleQueueLifecycle(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveSimpleQueue(SimpleQueue simpleQueue) {
        HashMap<UUID, SimpleQueue> existingQueues = loadSimpleQueues();
        existingQueues.put(simpleQueue.getId(), simpleQueue);
        saveQueuesToFile(SIMPLE_QUEUES_FILE_NAME, existingQueues);
    }

    public SimpleQueue getSimpleQueue(UUID uuid) {
        HashMap<UUID, SimpleQueue> existingQueues = loadSimpleQueues();
        return existingQueues.get(uuid);
    }

    public void deleteSimpleQueue(SimpleQueue simpleQueue) {
        HashMap<UUID, SimpleQueue> existingQueues = loadSimpleQueues();
        existingQueues.remove(simpleQueue.getId());
        saveQueuesToFile(SIMPLE_QUEUES_FILE_NAME, existingQueues);
    }

    public SimpleQueue createSimpleQueue() {
        SimpleQueue simpleQueue = new SimpleQueue();
        saveSimpleQueue(simpleQueue);
        return simpleQueue;
    }

    public SimpleQueue createSimpleQueue(String alias) {
        SimpleQueue simpleQueue = new SimpleQueue(alias);
        saveSimpleQueue(simpleQueue);
        return simpleQueue;
    }

    private <T> void saveQueuesToFile(String fileName, T data) {
        try {
            objectMapper.writeValue(new File(fileName), data);
        } catch (IOException e) {
            throw new RuntimeException("Error saving data to file", e);
        }
    }

    private HashMap<UUID, SimpleQueue> loadSimpleQueues() {
        File file = new File(SIMPLE_QUEUES_FILE_NAME);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<HashMap<UUID, SimpleQueue>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error loading simple queues data from file", e);
        }
    }
}
