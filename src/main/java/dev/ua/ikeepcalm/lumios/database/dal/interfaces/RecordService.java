package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordService {

    void save(MessageRecord messageRecord);

    void delete(MessageRecord messageRecord);

    MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException;

    List<MessageRecord> findLastMessagesByChatId(Long chatId, int number);

    int countAllByChatId(Long chatId);

    List<MessageRecord> findAllByChatAndDateBetween(LumiosChat chat, LocalDateTime startDate, LocalDateTime endDate);
}
