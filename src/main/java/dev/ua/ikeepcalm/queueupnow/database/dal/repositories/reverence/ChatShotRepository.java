package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ReverenceChatShot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatShotRepository extends CrudRepository<ReverenceChatShot, Long> {
    Optional<ReverenceChatShot> findByReverenceChat(ReverenceChat reverenceChat);
}