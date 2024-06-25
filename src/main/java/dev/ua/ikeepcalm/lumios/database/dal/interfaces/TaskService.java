package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;

import java.util.InputMismatchException;
import java.util.List;

public interface TaskService {

    void save(DueTask dueTask);

    void delete(DueTask dueTask);

    void deleteAllByChat(LumiosChat chat);

    boolean existsByChatAndTaskName(LumiosChat chat, String taskName);

    List<DueTask> getTasksForCurrentChat(LumiosChat chat);

    DueTask findTaskById(Long chatId, Long id) throws InputMismatchException;
}

