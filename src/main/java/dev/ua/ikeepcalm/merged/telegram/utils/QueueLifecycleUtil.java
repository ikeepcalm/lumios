package dev.ua.ikeepcalm.merged.telegram.utils;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

@Component
public class QueueLifecycleUtil {
    private final HashMap<UUID, QueueItself> allQueues;

    public QueueLifecycleUtil() {
        this.allQueues = loadHashMapFromFile();
    }

    public void saveHashMapToFile() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("queueData.ser"));
            try {
                outputStream.writeObject(this.allQueues);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } outputStream.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    private HashMap<UUID, QueueItself> loadHashMapFromFile() {
        File file = new File("queueData.ser");
        if (!file.exists()) {
            return new HashMap<>();
        } try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            return (HashMap<UUID, QueueItself>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public QueueItself createQueue() {
        QueueItself queueItself = new QueueItself();
        allQueues.put(queueItself.getId(), queueItself);
        return queueItself;
    }

    public QueueItself createQueue(String alias) {
        QueueItself queueItself = new QueueItself(alias);
        allQueues.put(queueItself.getId(), queueItself);
        return queueItself;
    }

    public void updateQueue(QueueItself queueItself) {
        this.allQueues.put(queueItself.getId(), queueItself);
    }

    public QueueItself getQueue(UUID uuid) {
        return allQueues.get(uuid);
    }

    public void deleteQueue(QueueItself queueItself) {
        allQueues.remove(queueItself);
    }
}