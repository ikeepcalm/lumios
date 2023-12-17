package dev.ua.ikeepcalm.queueupnow.database.dal.impls;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.ChatRepository;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public ReverenceChat findByChatId(long chatId) throws NoSuchEntityException{
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
        this.chatRepository.save(chat);
    }

    @Override
    public Iterable<ReverenceChat> findAll() {
        return chatRepository.findAll();
    }
}

