package dev.ua.ikeepcalm.lumios.database.dal.repositories.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimpleUserRepository extends CrudRepository<SimpleUser, Long> {
    List<SimpleUser> findBySimpleQueueId(UUID simpleQueueId);
}

