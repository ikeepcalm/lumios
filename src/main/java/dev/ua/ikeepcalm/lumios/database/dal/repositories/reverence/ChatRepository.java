package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository
        extends CrudRepository<LumiosChat, Long> {
    Optional<LumiosChat> findByChatId(long var1);
}

