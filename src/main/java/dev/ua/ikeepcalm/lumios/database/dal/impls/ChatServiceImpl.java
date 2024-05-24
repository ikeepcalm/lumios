package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.history.MessageRecordRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.ChatRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
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
    public ReverenceChat findByChatId(long chatId) throws NoSuchEntityException {
        Optional<ReverenceChat> reverenceChat = this.chatRepository.findByChatId(chatId);
        if (reverenceChat.isPresent()) {
            return reverenceChat.get();
        } else {
            throw new NoSuchEntityException("No such chat with id: " + chatId);
        }
    }

    @Override
    public void save(ReverenceChat chat) {
        this.chatRepository.save(chat);
    }

    @Override
    public void delete(ReverenceChat chat) {
        this.chatRepository.delete(chat);
    }

    @Override
    public Iterable<ReverenceChat> findAll() {
        return chatRepository.findAll();
    }
}

