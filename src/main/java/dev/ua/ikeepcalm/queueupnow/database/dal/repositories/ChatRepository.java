
package dev.ua.ikeepcalm.queueupnow.database.dal.repositories;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository
extends CrudRepository<ReverenceChat, Long> {
    Optional<ReverenceChat> findByChatId(long var1);
}

