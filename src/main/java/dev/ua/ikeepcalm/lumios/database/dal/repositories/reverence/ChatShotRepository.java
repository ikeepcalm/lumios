package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChatShotRepository extends CrudRepository<ChatShot, Long> {
    Optional<ChatShot> findByReverenceChat(ReverenceChat reverenceChat);

    Optional<ChatShot> findByReverenceChat_ChatIdAndDate(Long chatId, LocalDate date);
}