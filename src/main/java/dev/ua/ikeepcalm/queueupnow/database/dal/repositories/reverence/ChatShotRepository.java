package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ChatShot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChatShotRepository extends CrudRepository<ChatShot, Long> {
    Optional<ChatShot> findByReverenceChat(ReverenceChat reverenceChat);
    Optional<ChatShot> findByReverenceChatIdAndDate(Long chatId, LocalDate date);
}