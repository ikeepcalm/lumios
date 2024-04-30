package dev.ua.ikeepcalm.lumios.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

        List<SimpleQueue> queues = queueService.findAllSimpleByChatId(reverenceChat.getChatId());
        List<MixedQueue> mixedQueues = queueService.findAllMixedByChatId(reverenceChat.getChatId());
        if (queues.isEmpty() && mixedQueues.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        List<QueueWrapper> timetableWrappers = QueueWrapper.wrapQueues(queues, mixedQueues);
        return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueWrapper> getQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
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
    public ResponseEntity<String> createQueue(@RequestHeader("chatId") Long chatId, @RequestBody Map<String, String> body) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }
        String name = body.get("name");
        if (Boolean.parseBoolean(body.get("mixed"))) {
            MixedQueue queue = new MixedQueue(name);
            queue.setChatId(reverenceChat.getChatId());
            queueService.save(queue);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given queue!");
        } else {
            SimpleQueue queue = new SimpleQueue(name);
            queue.setChatId(reverenceChat.getChatId());
            queueService.save(queue);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given queue!");
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
            QueueWrapper queue = QueueParser.parseQueueMessage(json);
            if (queue.isMixed()) {
                MixedQueue mixedQueue = new MixedQueue(queue);
                mixedQueue.setChatId(reverenceChat.getChatId());
                queueService.deleteMixedQueue(mixedQueue);
                queueService.save(mixedQueue);
            } else {
                SimpleQueue simpleQueue = new SimpleQueue(queue);
                simpleQueue.setChatId(reverenceChat.getChatId());
                queueService.deleteSimpleQueue(simpleQueue);
                queue.setChatId(reverenceChat.getChatId());
                queueService.save(simpleQueue);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully updated queue!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @DeleteMapping("/simple/{id}")
    public ResponseEntity<String> deleteSimpleQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
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

    @DeleteMapping("/mixed/{id}")
    public ResponseEntity<String> deleteMixedQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            queueService.deleteMixedQueue(queueService.findMixedById(id));
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted queue!");
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Queue with ID: " + id + " is not found");
        }
    }

}
