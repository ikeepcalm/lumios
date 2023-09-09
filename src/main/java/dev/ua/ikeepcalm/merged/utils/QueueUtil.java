/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package dev.ua.ikeepcalm.merged.utils;

import dev.ua.ikeepcalm.merged.entities.queue.QueueItself;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

@Component
public class QueueUtil {
    private final HashMap<UUID, QueueItself> allQueues = this.loadHashMapFromFile();

    public QueueUtil() {
    }

    public void saveHashMapToFile() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("queueData.ser"));

            try {
                outputStream.writeObject(this.allQueues);
            } catch (Throwable var5) {
                try {
                    outputStream.close();
                } catch (Throwable var4) {
                    var5.addSuppressed(var4);
                }

                throw var5;
            }

            outputStream.close();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

    }

    private HashMap<UUID, QueueItself> loadHashMapFromFile() {
        File file = new File("queueData.ser");
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            return (HashMap<UUID, QueueItself>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public QueueItself createQueue(long chatId) {
        QueueItself queueItself = new QueueItself();
        this.allQueues.put(queueItself.getId(), queueItself);
        return queueItself;
    }

    public QueueItself createQueue(long chatId, String alias) {
        QueueItself queueItself = new QueueItself(alias);
        this.allQueues.put(queueItself.getId(), queueItself);
        return queueItself;
    }

    public void updateQueue(QueueItself queueItself) {
        UUID givenUUID = queueItself.getId();
        this.allQueues.put(givenUUID, queueItself);
    }

    public QueueItself getQueue(UUID uuid) {
        return (QueueItself)this.allQueues.get(uuid);
    }

    public void deleteQueue(QueueItself queueItself) {
        this.allQueues.remove(queueItself);
    }
}
