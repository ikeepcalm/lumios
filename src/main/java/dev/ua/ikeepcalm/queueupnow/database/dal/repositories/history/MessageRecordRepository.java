package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.history;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRecordRepository extends CrudRepository<MessageRecord, UUID> {
    Optional<MessageRecord> findByMessageIdAndChatId(Long id, Long chatId);

}

