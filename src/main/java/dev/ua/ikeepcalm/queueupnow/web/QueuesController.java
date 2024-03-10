package dev.ua.ikeepcalm.queueupnow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.queues.utils.QueueParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queues")
public class QueuesController {

    private final QueueService queueService;
    private final ChatService chatService;

    public QueuesController(QueueService taskService, ChatService chatService) {
        this.queueService = taskService;
        this.chatService = chatService;
    }

    @GetMapping("/retrieve")
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

    @GetMapping("/create")
    public ResponseEntity<String> createQueues(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            List<SimpleQueue> queues = QueueParser.parseQueueMessage(json);

            if (queues.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format! No queues found!");

            for (SimpleQueue q : queues) {
                q.setChatId(reverenceChat.getChatId());
                queueService.save(q);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given list of queues!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @GetMapping("/update")
    public ResponseEntity<String> updateQueues(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            List<SimpleQueue> queues = QueueParser.parseQueueMessage(json);

            if (queues.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format! No queues found!");

            List<SimpleQueue> savedQueues = queueService.findAllSimpleByChatId(reverenceChat.getChatId());

            for (SimpleQueue q : queues) {
                for (SimpleQueue savedQueue : savedQueues) {
                    if (q.getChatId() == savedQueue.getChatId() && q.getMessageId() == savedQueue.getMessageId()) {
                        q.setId(savedQueue.getId());
                        break;
                    }
                }
                q.setChatId(reverenceChat.getChatId());
                queueService.save(q);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given list of queues!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }



}
