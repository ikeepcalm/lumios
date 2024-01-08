package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<ReverenceUser, Long> {
    Optional<ReverenceUser> findReverenceUserByUserIdAndChannel(long var1, ReverenceChat var3);

    ReverenceUser findReverenceUserByUsernameAndChannel(String var1, ReverenceChat var2);

    List<ReverenceUser> findAllByChannel(ReverenceChat var1);
}

