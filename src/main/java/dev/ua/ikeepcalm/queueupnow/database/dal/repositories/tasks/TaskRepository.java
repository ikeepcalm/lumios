package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.tasks;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends CrudRepository<DueTask, Long> {
    List<DueTask> findByChat(ReverenceChat chat);
    Optional<DueTask> findByChatAndId(ReverenceChat chat, Long id);
    boolean existsByChatAndTaskName(ReverenceChat chat, String taskName);

}

