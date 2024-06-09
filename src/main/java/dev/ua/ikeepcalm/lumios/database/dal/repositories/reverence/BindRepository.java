package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceBind;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BindRepository extends CrudRepository<ReverenceBind, Long> {

    Optional<ReverenceBind> findByChatId(Long chatId);

    Optional<ReverenceBind> findByUserId(Long userId);

    void deleteByUserId(Long userId);

}
