package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.history;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRecordRepository extends CrudRepository<MessageRecord, UUID> {
    Optional<MessageRecord> findByMessageIdAndChatId(Long id, Long chatId);
    List<MessageRecord> findAllByChatIdAndDateBetween(Long chatId, LocalDate startDate, LocalDate endDate);

}

