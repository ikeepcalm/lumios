package dev.ua.ikeepcalm.queueupnow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.wrappers.TaskWrapper;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.tasks.utils.TaskParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.InputMismatchException;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TasksController {

    private final TaskService taskService;
    private final ChatService chatService;

    public TasksController(TaskService taskService, ChatService chatService) {
        this.taskService = taskService;
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<TaskWrapper>> getTasks(@RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;

        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<DueTask> tasks = taskService.getTasksForCurrentChat(reverenceChat);
        return ResponseEntity.status(HttpStatus.OK).body(TaskWrapper.wrapTasks(tasks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskWrapper> getTask(@RequestHeader("chatId") Long chatId, @PathVariable Long id) {
        ReverenceChat reverenceChat;

        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            DueTask task = taskService.findTaskById(reverenceChat.getChatId(), id);
            return ResponseEntity.status(HttpStatus.OK).body(TaskWrapper.wrapTask(task));
        } catch (InputMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

    @PutMapping
    public ResponseEntity<String> updateTasks(@RequestBody String json, @RequestHeader("chatId") Long chatId, @RequestHeader("taskId") Long taskId){
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            DueTask task = TaskParser.parseTaskMessage(json);

            DueTask existingTask = taskService.findTaskById(reverenceChat.getChatId(), taskId);
            existingTask.setTaskName(task.getTaskName());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setDueTime(task.getDueTime());
            existingTask.setUrl(task.getUrl());
            taskService.save(existingTask);

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully updated given task!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @PostMapping
    public ResponseEntity<String> createTasks(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            DueTask task = TaskParser.parseTaskMessage(json);
            task.setChat(reverenceChat);
            taskService.save(task);

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created given task!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@RequestHeader("chatId") Long chatId, @PathVariable("id") Long taskId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            DueTask task = taskService.findTaskById(reverenceChat.getChatId(), taskId);
            taskService.delete(task);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted task with ID: " + taskId);
        } catch (InputMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task with ID: " + taskId + " is not found");
        }
    }

}
