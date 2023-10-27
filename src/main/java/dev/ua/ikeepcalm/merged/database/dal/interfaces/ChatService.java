package dev.ua.ikeepcalm.merged.database.dal.interfaces;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;

public interface ChatService {
    ReverenceChat find(long chatId);

    void save(ReverenceChat chat);

    void delete(ReverenceChat chat);

    Iterable<ReverenceChat> findAll();
}

