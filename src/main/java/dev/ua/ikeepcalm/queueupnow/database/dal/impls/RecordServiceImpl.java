package dev.ua.ikeepcalm.queueupnow.database.dal.impls;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.history.MessageRecordRepository;
import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecordServiceImpl implements RecordService {

    private final MessageRecordRepository messageRecordRepository;

    public RecordServiceImpl(MessageRecordRepository messageRecordRepository) {
        this.messageRecordRepository = messageRecordRepository;
    }

    @Override
    public void save(MessageRecord messageRecord) {
        this.messageRecordRepository.save(messageRecord);
    }

    @Override
    public void delete(MessageRecord messageRecord) {
        this.messageRecordRepository.delete(messageRecord);
    }

    @Override
    public MessageRecord findByMessageIdAndChatId(Long id, Long chatId) throws NoSuchEntityException {
        return this.messageRecordRepository.findByMessageIdAndChatId(id, chatId).orElseThrow(() -> new NoSuchEntityException("No such record with id: " + id));
    }

    @Override
    public List<MessageRecord> findAllByChatIdAndDateBetween(Long chatId, LocalDate startDate, LocalDate endDate) {
        return this.messageRecordRepository.findAllByChatIdAndDateBetween(chatId, startDate, endDate);
    }

}
