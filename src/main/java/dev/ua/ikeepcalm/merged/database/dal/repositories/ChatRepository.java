
package dev.ua.ikeepcalm.merged.database.dal.repositories;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository
extends CrudRepository<ReverenceChat, Long> {
    ReverenceChat findByChatId(long var1);
}

