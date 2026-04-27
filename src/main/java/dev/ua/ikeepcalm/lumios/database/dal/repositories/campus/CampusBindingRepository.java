package dev.ua.ikeepcalm.lumios.database.dal.repositories.campus;

import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampusBindingRepository extends JpaRepository<CampusBinding, Long> {

    Optional<CampusBinding> findByTelegramUserId(Long telegramUserId);

    Optional<CampusBinding> findByExternalId(String externalId);

    void deleteByTelegramUserId(Long telegramUserId);

    boolean existsByTelegramUserId(Long telegramUserId);

}
