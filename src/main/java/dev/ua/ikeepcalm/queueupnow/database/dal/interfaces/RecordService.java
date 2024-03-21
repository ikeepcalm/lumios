package dev.ua.ikeepcalm.queueupnow.database.dal.interfaces;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;

import java.time.LocalDate;
import java.util.List;

public interface RecordService {

    void save(MessageRecord messageRecord);

    void delete(MessageRecord messageRecord);

    MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException;

    List<MessageRecord> findAllByChatIdAndDateBetween(Long chatId, LocalDate startDate, LocalDate endDate);
}
