package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MixedUserRepository extends CrudRepository<MixedUser, Long> {

}

