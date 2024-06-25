package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<LumiosUser, Long> {
    Optional<LumiosUser> findReverenceUserByUserIdAndChat(long userId, LumiosChat channel);

    List<LumiosUser> findByUserId(long userId);

    LumiosUser findReverenceUserByUsernameAndChat(String username, LumiosChat channel);

    List<LumiosUser> findAllByChat(LumiosChat channel);
}

