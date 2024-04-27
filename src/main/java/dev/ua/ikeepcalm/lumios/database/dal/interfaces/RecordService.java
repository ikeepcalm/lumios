package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.time.LocalDate;
import java.util.List;

public interface RecordService {

    void save(MessageRecord messageRecord);

    void delete(MessageRecord messageRecord);

    MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException;

    List<MessageRecord> findAllByChatIdAndDateBetween(Long chatId, LocalDate startDate, LocalDate endDate);
}
