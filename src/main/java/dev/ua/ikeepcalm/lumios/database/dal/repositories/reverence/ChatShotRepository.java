package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatShotRepository extends CrudRepository<ChatShot, Long> {
    Optional<ChatShot> findByChat(LumiosChat lumiosChat);

    List<ChatShot> findAllByChat_ChatIdAndDate(Long chatId, LocalDate date);
}