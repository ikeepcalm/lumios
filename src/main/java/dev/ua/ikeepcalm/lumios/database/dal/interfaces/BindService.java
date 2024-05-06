package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceBind;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;

public interface BindService {

    void save(ReverenceBind reverenceBind);

    ReverenceBind findByChatId(Long chatId) throws NoSuchEntityException;

    ReverenceBind findByUserId(Long userId) throws NoSuchEntityException;

    void deleteByUserId(Long userId);

}
