package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ReverenceUserShot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserShotRepository extends CrudRepository<ReverenceUserShot, Long> {
    Optional<ReverenceUserShot> findByUserId(long userId);
}