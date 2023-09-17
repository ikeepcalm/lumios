
package dev.ua.ikeepcalm.merged.dal.repositories;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository
extends CrudRepository<ReverenceChat, Long> {
    public ReverenceChat findByChatId(long var1);
}

