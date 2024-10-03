package dev.ua.ikeepcalm.lumios.database.dal.repositories.history;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRecordRepository extends CrudRepository<MessageRecord, UUID> {
    Optional<MessageRecord> findByMessageIdAndChatId(Long id, Long chatId);

    List<MessageRecord> findAllByChatIdAndDateBetween(Long chatId, LocalDateTime date, LocalDateTime date2);

    List<MessageRecord> findByChatIdOrderByDateDesc(Long chatId, Pageable pageable);

    int countAllByChatId(Long chatId);

    void deleteAllByUser(LumiosUser user);

}

