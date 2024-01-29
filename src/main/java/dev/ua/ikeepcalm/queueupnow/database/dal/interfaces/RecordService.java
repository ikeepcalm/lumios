package dev.ua.ikeepcalm.queueupnow.database.dal.interfaces;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;

public interface RecordService {

    void save(MessageRecord messageRecord);

    void delete(MessageRecord messageRecord);

    MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException;
}
