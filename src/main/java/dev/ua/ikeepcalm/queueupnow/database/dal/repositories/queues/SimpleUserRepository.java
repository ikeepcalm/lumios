package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimpleUserRepository extends CrudRepository<SimpleUser, Long> {
    List<SimpleUser> findBySimpleQueueId(UUID simpleQueueId);
}

