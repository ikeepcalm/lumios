package dev.ua.ikeepcalm.queueupnow.database.dal.interfaces;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;

import java.util.InputMismatchException;
import java.util.List;

public interface TaskService {

    void save(DueTask dueTask);
    void delete(DueTask dueTask);
    boolean existsByChatAndTaskName(ReverenceChat chat, String taskName);
    List<DueTask> getTasksForCurrentChat(ReverenceChat chat);
    DueTask findTaskById(Long chatId, Long id) throws InputMismatchException;
}

