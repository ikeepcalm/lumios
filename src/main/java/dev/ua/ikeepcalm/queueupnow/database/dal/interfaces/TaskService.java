package dev.ua.ikeepcalm.queueupnow.database.dal.interfaces;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;

import java.util.List;

public interface TaskService {

    void save(DueTask dueTask);

    List<DueTask> getTasksForCurrentChat(ReverenceChat chat);

    DueTask findTaskById(Long id);
}

