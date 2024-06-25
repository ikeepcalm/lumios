package dev.ua.ikeepcalm.lumios.web.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/queues")
public class QueuesController {

    private final ChatService chatService;
    private final UserService userService;
    private final QueueService queueService;
    private final TelegramClient telegramClient;

    public QueuesController(ChatService chatService, UserService userService, QueueService queueService, TelegramClient telegramClient) {
        this.chatService = chatService;
        this.userService = userService;
        this.queueService = queueService;
        this.telegramClient = telegramClient;
    }

    @GetMapping
    public ResponseEntity<List<QueueWrapper>> getQueues(@RequestHeader("chatId") Long chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<SimpleQueue> queues = queueService.findAllSimpleByChatId(lumiosChat.getChatId());
        List<MixedQueue> mixedQueues = queueService.findAllMixedByChatId(lumiosChat.getChatId());
        if (queues.isEmpty() && mixedQueues.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
        }
        List<QueueWrapper> timetableWrappers = QueueWrapper.wrapQueues(queues, mixedQueues);
        return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueWrapper> getQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
        LumiosChat lumiosChat;
        try {
            chatService.findByChatId(chatId);
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

    @PostMapping("/shuffle/{id}")
    public ResponseEntity<QueueWrapper> shuffleQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id) {
        LumiosChat lumiosChat;
        try {
            chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        MixedQueue mixedQueue;
        try {
            mixedQueue = queueService.findMixedById(id);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        mixedQueue.shuffleContents();
        SimpleQueue simpleQueue = new SimpleQueue();
        simpleQueue.setId(mixedQueue.getId());
        simpleQueue.setMessageId(mixedQueue.getMessageId());
        simpleQueue.setAlias(mixedQueue.getAlias());
        simpleQueue.setChatId(mixedQueue.getChatId());
        for (MixedUser mixedUser : mixedQueue.getContents()) {
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setId(mixedUser.getId());
            simpleUser.setName(mixedUser.getName());
            simpleUser.setUsername(mixedUser.getUsername());
            simpleUser.setAccountId(mixedUser.getAccountId());
            simpleUser.setSimpleQueue(simpleQueue);
            simpleQueue.getContents().add(simpleUser);
        }
        queueService.deleteMixedQueue(mixedQueue);
        simpleQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(chatId, simpleQueue)).getMessageId());
        queueService.save(simpleQueue);
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chatId);
        textMessage.setText("Successfully shuffled queue " + simpleQueue.getAlias() + "!");
        telegramClient.sendTextMessage(textMessage);
        return ResponseEntity.status(HttpStatus.OK).body(QueueWrapper.wrapQueue(simpleQueue));
    }

    @PostMapping
    public ResponseEntity<String> createQueue(@RequestHeader("chatId") Long chatId, @RequestBody Map<String, String> body) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }
        String name = body.get("name");
        if (Boolean.parseBoolean(body.get("mixed"))) {
            MixedQueue queue = new MixedQueue(name);
            TextMessage queueMessage = new TextMessage();
            queueMessage.setChatId(chatId);
            queueMessage.setText(">>> " + name + " <<<\n\n");
            queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queue));
            Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
            try {
                this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
            } catch (TelegramApiException e) {
                TextMessage textMessage = new TextMessage();
                textMessage.setChatId(chatId);
                textMessage.setText("Якщо ви хочете, щоб ця черга була закріплена, надайте боту відповідні права!");
                telegramClient.sendTextMessage(textMessage);
            }
            queue.setMessageId(sendTextMessage.getMessageId());
            queue.setChatId(lumiosChat.getChatId());
            queueService.save(queue);
        } else {
            SimpleQueue queue = new SimpleQueue(name);
            TextMessage queueMessage = new TextMessage();
            queueMessage.setChatId(chatId);
            queueMessage.setText(">>> " + name + " <<<\n\n");
            queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(queue));
            Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
            try {
                this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
            } catch (TelegramApiException e) {
                TextMessage textMessage = new TextMessage();
                textMessage.setChatId(chatId);
                textMessage.setText("Якщо ви хочете, щоб ця черга була закріплена, надайте боту відповідні права!");
                telegramClient.sendTextMessage(textMessage);
            }
            queue.setMessageId(sendTextMessage.getMessageId());
            queue.setChatId(lumiosChat.getChatId());
            queueService.save(queue);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved given queue!");
    }

    @PutMapping
    public ResponseEntity<String> updateQueue(@RequestBody String json, @RequestHeader("chatId") Long chatId, @RequestHeader("userId") Long userId) {
        LumiosChat lumiosChat;
        LumiosUser lumiosUser;
        try {
            lumiosChat = chatService.findByChatId(chatId);
            lumiosUser = userService.findById(userId, lumiosChat);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat or User with ID: " + chatId + " is not registered in the system");
        }

        try {
            QueueWrapper queue = QueueParser.parseQueueMessage(json);
            if (queue.isMixed()) {
                MixedQueue mixedQueue = new MixedQueue(queue);
                queueService.deleteMixedQueue(mixedQueue);
                mixedQueue.setChatId(chatId);
                mixedQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(lumiosChat.getChatId(), mixedQueue)).getMessageId());
                queueService.save(mixedQueue);
            } else {
                SimpleQueue simpleQueue = new SimpleQueue(queue);
                queueService.deleteSimpleQueue(simpleQueue);
                simpleQueue.setChatId(chatId);
                simpleQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(lumiosChat.getChatId(), simpleQueue)).getMessageId());
                queueService.save(simpleQueue);
            }

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(lumiosChat.getChatId());
            textMessage.setMessageId(queue.getMessageId());
            textMessage.setText("Ця черга (" + queue.getAlias() + ") щойно була оновлена @" + lumiosUser.getUsername() + "!");
            telegramClient.sendTextMessage(textMessage);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully updated queue!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @DeleteMapping("/simple/{id}")
    public ResponseEntity<String> deleteSimpleQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id, @RequestHeader("userId") Long userId) {
        LumiosChat lumiosChat;
        LumiosUser lumiosUser;
        try {
            lumiosChat = chatService.findByChatId(chatId);
            lumiosUser = userService.findById(userId, lumiosChat);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat or User with ID: " + chatId + " is not registered in the system");
        }

        try {
            SimpleQueue simpleQueue = queueService.findSimpleById(id);
            queueService.deleteSimpleQueue(simpleQueue);
            try {
                telegramClient.sendRemoveMessage(new RemoveMessage(simpleQueue.getMessageId(), chatId));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(chatId);
            textMessage.setText("Черга (" + simpleQueue.getAlias() + ") щойно була видалена @" + lumiosUser.getUsername() + "!");
            telegramClient.sendTextMessage(textMessage);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted queue!");
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Queue with ID: " + id + " is not found");
        }
    }

    @DeleteMapping("/mixed/{id}")
    public ResponseEntity<String> deleteMixedQueue(@RequestHeader("chatId") Long chatId, @PathVariable UUID id, @RequestHeader("userId") Long userId) {
        LumiosChat lumiosChat;
        LumiosUser lumiosUser;
        try {
            lumiosChat = chatService.findByChatId(chatId);
            lumiosUser = userService.findById(userId, lumiosChat);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat or User with ID: " + chatId + " is not registered in the system");
        }

        try {
            MixedQueue mixedQueue = queueService.findMixedById(id);
            queueService.deleteMixedQueue(mixedQueue);
            telegramClient.sendRemoveMessage(new RemoveMessage(mixedQueue.getMessageId(), chatId));
            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(chatId);
            textMessage.setText("Черга (" + mixedQueue.getAlias() + ") щойно була видалена @" + lumiosUser.getUsername() + "!");
            telegramClient.sendTextMessage(textMessage);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted queue!");
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Queue with ID: " + id + " is not found");
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
