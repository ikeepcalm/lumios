package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimpleQueueRepository extends CrudRepository<SimpleQueue, UUID> {

    Optional<SimpleQueue> findById(UUID id);
    List<SimpleQueue> findAllByChatId(long chatId);

}

