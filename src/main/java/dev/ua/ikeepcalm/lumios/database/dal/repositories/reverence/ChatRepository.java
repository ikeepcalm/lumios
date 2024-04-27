package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository
        extends CrudRepository<ReverenceChat, Long> {
    Optional<ReverenceChat> findByChatId(long var1);
}

