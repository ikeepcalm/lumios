package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

public interface ChatService {
    ReverenceChat findByChatId(long chatId) throws NoSuchEntityException;

    void save(ReverenceChat chat);

    void delete(ReverenceChat chat);

    Iterable<ReverenceChat> findAll();
}

