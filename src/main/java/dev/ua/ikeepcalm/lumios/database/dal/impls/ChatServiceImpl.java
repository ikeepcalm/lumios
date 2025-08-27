package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.history.MessageRecordRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.ChatRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final MessageRecordRepository messageRecordRepository;

    public ChatServiceImpl(ChatRepository chatRepository, UserService userService, MessageRecordRepository messageRecordRepository) {
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.messageRecordRepository = messageRecordRepository;
    }

    @Override
    public LumiosChat findByChatId(long chatId) throws NoSuchEntityException {
        Optional<LumiosChat> reverenceChat = this.chatRepository.findByChatId(chatId);
        if (reverenceChat.isPresent()) {
//            reverenceChat.get().getUsers().size();
            return reverenceChat.get();
        } else {
            throw new NoSuchEntityException("No such chat with id: " + chatId);
        }
    }

    @Override
    public void save(LumiosChat chat) {
        this.chatRepository.save(chat);
    }

    @Override
    public void delete(LumiosChat chat) {
        this.chatRepository.delete(chat);
    }

    @Override
    public Iterable<LumiosChat> findAll() {
        return chatRepository.findAll();
    }
    
    @Override
    public int batchUpdateLimits(int summaryLimit, int communicationLimit) {
        return chatRepository.batchUpdateLimits(summaryLimit, communicationLimit);
    }
}

