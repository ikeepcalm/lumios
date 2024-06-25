package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;

import java.util.List;

public interface UserService {
    LumiosUser findById(long userId, LumiosChat chat) throws NoSuchEntityException;

    void updateAll();

    void increaseAll();

    List<LumiosUser> findById(long id);

    LumiosUser findByUsername(String var1, LumiosChat var2);

    List<LumiosUser> findAll(LumiosChat var1);

    void save(LumiosUser var1);

    void delete(LumiosUser var1);

    boolean checkIfUserExists(long var1, LumiosChat var3);

    boolean checkIfMentionedUserExists(String var1, LumiosChat var2);
}

