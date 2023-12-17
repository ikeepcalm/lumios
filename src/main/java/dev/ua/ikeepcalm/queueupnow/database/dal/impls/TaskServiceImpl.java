package dev.ua.ikeepcalm.queue.database.dal.impls;

import dev.ua.ikeepcalm.queue.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.queue.database.dal.repositories.TaskRepository;
import dev.ua.ikeepcalm.queue.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queue.database.entities.tasks.DueTask;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl
implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void save(DueTask dueTask) {
        taskRepository.save(dueTask);
    }

    @Override
    public List<DueTask> getTasksForCurrentChat(ReverenceChat chatId) {
        return taskRepository.findByChat(chatId);
    }

    @Override
    public DueTask findTaskById(Long id) throws InputMismatchException{
        Optional<DueTask> dueTask = taskRepository.findById(id);
        if (dueTask.isEmpty()){
            throw new InputMismatchException("Couldn't find DueTask by id " + id);
        } else {
            return dueTask.get();
        }
    }

}

