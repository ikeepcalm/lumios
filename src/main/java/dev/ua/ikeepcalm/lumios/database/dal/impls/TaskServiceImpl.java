package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.tasks.TaskRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl
        implements TaskService {

    private final TaskRepository taskRepository;
    private final ChatService chatService;

    public TaskServiceImpl(TaskRepository taskRepository, ChatService chatService) {
        this.taskRepository = taskRepository;
        this.chatService = chatService;
    }

    @Override
    public void save(DueTask dueTask) {
        taskRepository.save(dueTask);
    }

    @Override
    public void delete(DueTask dueTask) {
        taskRepository.delete(dueTask);
    }

    @Override
    public void deleteAllByChat(LumiosChat chat) {
        taskRepository.deleteAllByChat(chat);
    }

    @Override
    public boolean existsByChatAndTaskName(LumiosChat chat, String taskName) {
        return taskRepository.existsByChatAndTaskName(chat, taskName);
    }

    @Override
    public List<DueTask> getTasksForCurrentChat(LumiosChat chatId) {
        return taskRepository.findByChat(chatId);
    }

    @Override
    public DueTask findTaskById(Long chatId, Long id) throws InputMismatchException {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            throw new InputMismatchException("Couldn't find chat by id " + chatId);
        }
        Optional<DueTask> dueTask = taskRepository.findByChatAndId(chat, id);
        if (dueTask.isEmpty()) {
            throw new InputMismatchException("Couldn't find DueTask by id " + id);
        } else {
            return dueTask.get();
        }
    }

}

