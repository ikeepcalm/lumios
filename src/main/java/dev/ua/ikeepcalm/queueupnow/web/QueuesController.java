package dev.ua.ikeepcalm.queueupnow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/queues")
public class QueuesController {

    private final QueueService queueService;
    private final ChatService chatService;

    public QueuesController(QueueService taskService, ChatService chatService) {
        this.queueService = taskService;
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<QueueWrapper>> getQueues(@RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<QueueWrapper> timetableWrappers = QueueWrapper.wrapQueues(queueService.findAllSimpleByChatId(reverenceChat.getChatId()));
        return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueWrapper> getQueues(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        QueueWrapper timetableWrappers;
        try {
            timetableWrappers = QueueWrapper.wrapQueue(queueService.findSimpleById(id));
            return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PostMapping
    public ResponseEntity<String> createQueue(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            SimpleQueue queue = QueueParser.parseQueueMessage(json);
            queue.setChatId(reverenceChat.getChatId());
            queueService.save(queue);

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given queue!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @PutMapping
    public ResponseEntity<String> updateQueue(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            SimpleQueue queue = QueueParser.parseQueueMessage(json);
            queueService.deleteSimpleQueue(queue);
            queue.setChatId(reverenceChat.getChatId());
            queueService.save(queue);

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully updated queue!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            queueService.deleteSimpleQueue(queueService.findSimpleById(id));
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted queue!");
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Queue with ID: " + id + " is not found");
        }
    }

}
