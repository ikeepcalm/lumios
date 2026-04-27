package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

public interface CampusBindingService {

    void save(CampusBinding binding);

    CampusBinding findByTelegramUserId(Long telegramUserId) throws NoSuchEntityException;

    CampusBinding findByExternalId(String externalId) throws NoSuchEntityException;

    void deleteByTelegramUserId(Long telegramUserId);

    boolean existsByTelegramUserId(Long telegramUserId);

}
