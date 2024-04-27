package dev.ua.ikeepcalm.lumios.database.dal.repositories.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MixedQueueRepository extends CrudRepository<MixedQueue, UUID> {

    Optional<MixedQueue> findById(UUID id);

}

