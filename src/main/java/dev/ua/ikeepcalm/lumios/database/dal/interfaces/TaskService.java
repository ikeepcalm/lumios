package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;

import java.util.InputMismatchException;
import java.util.List;

public interface TaskService {

    void save(DueTask dueTask);

    void delete(DueTask dueTask);

    void deleteAllByChat(ReverenceChat chat);

    boolean existsByChatAndTaskName(ReverenceChat chat, String taskName);

    List<DueTask> getTasksForCurrentChat(ReverenceChat chat);

    DueTask findTaskById(Long chatId, Long id) throws InputMismatchException;
}

