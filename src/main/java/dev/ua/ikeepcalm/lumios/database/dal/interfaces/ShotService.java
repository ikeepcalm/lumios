package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;

import java.time.LocalDate;
import java.util.NoSuchElementException;

public interface ShotService {

    void save(ChatShot chatShot);

    ChatShot findByChatIdAndDate(Long chatId, LocalDate date) throws NoSuchElementException;
}
