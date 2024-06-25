package dev.ua.ikeepcalm.lumios.database.dal.repositories.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends CrudRepository<DueTask, Long> {
    List<DueTask> findByChat(LumiosChat chat);

    Optional<DueTask> findByChatAndId(LumiosChat chat, Long id);

    void deleteAllByChat(LumiosChat chat);

    boolean existsByChatAndTaskName(LumiosChat chat, String taskName);


}

