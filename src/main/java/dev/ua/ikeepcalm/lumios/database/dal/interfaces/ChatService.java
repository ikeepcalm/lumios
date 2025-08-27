package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

public interface ChatService {
    LumiosChat findByChatId(long chatId) throws NoSuchEntityException;

    void save(LumiosChat chat);

    void delete(LumiosChat chat);

    Iterable<LumiosChat> findAll();
    
    int batchUpdateLimits(int summaryLimit, int communicationLimit);
}

