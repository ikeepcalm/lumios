package dev.ua.ikeepcalm.lumios.database.dal.repositories.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MixedUserRepository extends CrudRepository<MixedUser, Long> {

}

