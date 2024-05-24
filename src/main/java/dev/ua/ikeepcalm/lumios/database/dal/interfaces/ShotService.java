package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.time.LocalDate;

public interface ShotService {

    void save(ChatShot chatShot);

    ChatShot findByChatIdAndDate(Long chatId, LocalDate date) throws NoSuchEntityException;

    void delete(ChatShot chatShot);
}
