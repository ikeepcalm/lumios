package dev.ua.ikeepcalm.lumios.telegram.core.shortcuts;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.*;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeCustomEmoji;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Transactional
public abstract class ServicesShortcut {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TelegramClient telegramClient;
    public ChatService chatService;
    public UserService userService;
    public TaskService taskService;
    public TimetableService timetableService;
    public QueueService queueService;
    public RecordService recordService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ServicesShortcut.class);

    @Autowired
    public void init(RecordService recordService, TelegramClient telegramClient, ChatService chatService, UserService userService, TaskService taskService, TimetableService timetableService, QueueService queueService) {
        this.telegramClient = telegramClient;
        this.recordService = recordService;
        this.chatService = chatService;
        this.userService = userService;
        this.taskService = taskService;
        this.timetableService = timetableService;
        this.queueService = queueService;
    }

    public Message sendMessage(String text, Message message) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setText(text);
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
        return sent;
    }

    public Message sendMessage(String text, String parseMode, Message message) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setParseMode(parseMode);
        textMessage.setText(text);
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
        return sent;
    }

    public void sendMessage(TextMessage textMessage, Message message) {
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void editMessage(EditMessage message) {
        telegramClient.sendEditMessage(message);
    }

    protected ReverenceReaction findNewReaction(List<ReactionType> oldList, List<ReactionType> newList) {
        ReverenceReaction newReaction = ReverenceReaction.DEFAULT;

        for (ReactionType iteReaction : newList) {
            if (!oldList.contains(iteReaction)) {
                if (iteReaction instanceof ReactionTypeEmoji) {
                    newReaction = ReverenceReaction.determineReaction(((ReactionTypeEmoji) iteReaction).getEmoji());
                } else if (iteReaction instanceof ReactionTypeCustomEmoji) {
                    newReaction = ReverenceReaction.PREMIUM;
                }
            }
        }
        return newReaction;
    }

    protected void scheduleMessageToDelete(Message message) {
        scheduler.schedule(() -> {
            RemoveMessage removeMessage = new RemoveMessage(message.getMessageId(), message.getChatId());
            try {
                telegramClient.sendRemoveMessage(removeMessage);
            } catch (TelegramApiException e) {
                logger.error("Failed to delete message: {}", message.getMessageId());
            }
        }, 5, TimeUnit.MINUTES);
    }


}
