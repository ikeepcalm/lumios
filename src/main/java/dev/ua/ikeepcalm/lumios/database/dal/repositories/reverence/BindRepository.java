package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosBind;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BindRepository extends CrudRepository<LumiosBind, Long> {

    Optional<LumiosBind> findByChatId(Long chatId);

    Optional<LumiosBind> findByUserId(Long userId);

    void deleteByUserId(Long userId);

}
