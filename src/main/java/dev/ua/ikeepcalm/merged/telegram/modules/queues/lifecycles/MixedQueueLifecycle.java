package dev.ua.ikeepcalm.merged.telegram.modules.queues.lifecycles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ua.ikeepcalm.merged.database.entities.queue.MixedQueue;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Component
public class MixedQueueLifecycle {
    private final ObjectMapper objectMapper;
    private static final String MIXED_QUEUES_FILE_NAME = "mixedQueues.json";

    public MixedQueueLifecycle(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveMixedQueue(MixedQueue mixedQueue) {
        HashMap<UUID, MixedQueue> existingMixedQueues = loadMixedQueues();
        existingMixedQueues.put(mixedQueue.getId(), mixedQueue);
        saveQueuesToFile(MIXED_QUEUES_FILE_NAME, existingMixedQueues);
    }

    public MixedQueue getMixedQueue(UUID uuid) {
        HashMap<UUID, MixedQueue> existingMixedQueues = loadMixedQueues();
        return existingMixedQueues.get(uuid);
    }

    public MixedQueue createMixedQueue() {
        MixedQueue mixedQueue = new MixedQueue();
        saveMixedQueue(mixedQueue);
        return mixedQueue;
    }

    public MixedQueue createMixedQueue(String alias) {
        MixedQueue mixedQueue = new MixedQueue(alias);
        saveMixedQueue(mixedQueue);
        return mixedQueue;
    }

    public void deleteMixedQueue(MixedQueue simpleQueue) {
        HashMap<UUID, MixedQueue> existingQueues = loadMixedQueues();
        existingQueues.remove(simpleQueue.getId());
        saveQueuesToFile(MIXED_QUEUES_FILE_NAME, existingQueues);
    }

    private <T> void saveQueuesToFile(String fileName, T data) {
        try {
            objectMapper.writeValue(new File(fileName), data);
        } catch (IOException e) {
            throw new RuntimeException("Error saving data to file", e);
        }
    }

    private HashMap<UUID, MixedQueue> loadMixedQueues() {
        File file = new File(MIXED_QUEUES_FILE_NAME);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<HashMap<UUID, MixedQueue>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error loading mixed queues data from file", e);
        }
    }
}
