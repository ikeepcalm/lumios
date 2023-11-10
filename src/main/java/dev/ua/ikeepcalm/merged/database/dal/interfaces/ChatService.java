package dev.ua.ikeepcalm.merged.database.dal.interfaces;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.exceptions.NoSuchEntityException;

public interface ChatService {
    ReverenceChat findByChatId(long chatId) throws NoSuchEntityException;

    void save(ReverenceChat chat);

    void delete(ReverenceChat chat);

    Iterable<ReverenceChat> findAll();
}

