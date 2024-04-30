package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordService {

    void save(MessageRecord messageRecord);

    void delete(MessageRecord messageRecord);

    MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException;

    List<MessageRecord> findAllByChatAndDateBetween(ReverenceChat chat, LocalDateTime startDate, LocalDateTime endDate);
}
