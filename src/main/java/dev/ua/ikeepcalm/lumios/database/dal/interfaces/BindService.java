package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosBind;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

public interface BindService {

    void save(LumiosBind lumiosBind);

    LumiosBind findByChatId(Long chatId) throws NoSuchEntityException;

    LumiosBind findByUserId(Long userId) throws NoSuchEntityException;

    void deleteByUserId(Long userId);

}
