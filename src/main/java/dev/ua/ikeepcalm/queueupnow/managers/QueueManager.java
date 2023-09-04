package dev.ua.ikeepcalm.queueupnow.managers;

import dev.ua.ikeepcalm.queueupnow.entities.Queue;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;


@Component
public class QueueManager {

    private final HashMap<UUID, Queue> allQueues;

    public QueueManager() {
        allQueues = new HashMap<>();
    }

    public Queue createQueue(long chatId){
        Queue queue = new Queue();
        allQueues.put(queue.getId(), queue);
        return queue;
    }

    public Queue createQueue(long chatId, String alias){
        Queue queue = new Queue(alias);
        allQueues.put(queue.getId(), queue);
        return queue;
    }

    public void updateQueue(Queue queue){
        UUID givenUUID = queue.getId();
        allQueues.put(givenUUID, queue);
    }

    public Queue getQueue(UUID uuid){
        return allQueues.get(uuid);
    }

    public void deleteQueue(Queue queue){
        allQueues.remove(queue);
    }

}
