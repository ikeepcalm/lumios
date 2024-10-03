package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.history.MessageRecordRepository;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    public List<MessageRecord> findLastMessagesByChatId(Long chatId, int number) {
        PageRequest pageRequest = PageRequest.of(0, number);
        return messageRecordRepository.findByChatIdOrderByDateDesc(chatId, pageRequest);
    }

    @Override
    public int countAllByChatId(Long chatId) {
        return this.messageRecordRepository.countAllByChatId(chatId);
    }

    @Override
    public List<MessageRecord> findAllByChatAndDateBetween(LumiosChat chat, LocalDateTime startDate, LocalDateTime endDate) {
        return this.messageRecordRepository.findAllByChatIdAndDateBetween(chat.getChatId(), startDate, endDate);
    }

}
