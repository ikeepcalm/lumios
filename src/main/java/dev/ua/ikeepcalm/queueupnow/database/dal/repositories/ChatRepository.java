
package dev.ua.ikeepcalm.queue.database.dal.repositories;

import dev.ua.ikeepcalm.queue.database.entities.reverence.ReverenceChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository
extends CrudRepository<ReverenceChat, Long> {
    Optional<ReverenceChat> findByChatId(long var1);
}

