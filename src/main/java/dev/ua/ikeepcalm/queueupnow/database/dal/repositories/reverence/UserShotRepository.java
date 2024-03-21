package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.UserShot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserShotRepository extends CrudRepository<UserShot, Long> {
    Optional<UserShot> findByUserId(long userId);
}