package dev.ua.ikeepcalm.merged.database.dal.repositories;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.tasks.DueTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<DueTask, Long> {
    List<DueTask> findByChat(ReverenceChat chat);
}
