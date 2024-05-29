package dev.ua.ikeepcalm.lumios.telegram.modules.parents;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.*;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.ReactionMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public abstract class CommandParent {

    protected Message message;
    protected TelegramClient telegramClient;
    protected ReverenceChat reverenceChat;
    protected ReverenceUser reverenceUser;
    protected ChatService chatService;
    protected UserService userService;
    protected TaskService taskService;
    protected TimetableService timetableService;
    protected QueueService queueService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Logger logger;

    @Autowired
    private void setupDependencies(TelegramClient telegramClient,
                                   ChatService chatService,
                                   UserService userService,
                                   TaskService taskService,
                                   TimetableService timetableService,
                                   QueueService queueService) {
        this.telegramClient = telegramClient;
        this.chatService = chatService;
        this.userService = userService;
        this.taskService = taskService;
        this.timetableService = timetableService;
        this.queueService = queueService;
        this.logger = LoggerFactory.getLogger(CommandParent.class);
    }

    @Transactional
    public void handleUpdate(Message message) {
        this.message = message;
        ChatMember chatMember = telegramClient.getChatMember(message.getChatId(), message.getFrom().getId());
        if (chatMember.getStatus().equals(ChatMemberRestricted.STATUS)) {
            return;
        }
        try {
            this.reverenceChat = chatService.findByChatId(message.getChatId());
            this.reverenceChat.setName(message.getChat().getTitle());
            this.reverenceChat.setDescription(message.getChat().getDescription());
        } catch (NoSuchEntityException e) {
            ReverenceChat newChat = new ReverenceChat();
            newChat.setChatId(message.getChatId());
            newChat.setName(message.getChat().getTitle());
            this.chatService.save(newChat);
            this.reverenceChat = newChat;
            return;
        }
        if (!message.getFrom().getIsBot()) {
            try {
                this.reverenceUser = this.userService.findById(message.getFrom().getId(), reverenceChat);
                if (!Objects.equals(reverenceUser.getUsername(), message.getFrom().getUserName())) {
                    reverenceUser.setUsername(message.getFrom().getUserName());
                    userService.save(reverenceUser);
                }
            } catch (NoSuchEntityException e) {
                ReverenceUser newUser = new ReverenceUser();
                newUser.setUserId(message.getFrom().getId());
                newUser.setUsername(message.getFrom().getUserName());
                newUser.setCredits(100);
                newUser.setSustainable(100);
                newUser.setChat(reverenceChat);
                userService.save(newUser);
                reverenceUser = newUser;
            }
        }
        logInteraction();
        processUpdate(message);
    }

    protected abstract void processUpdate(Message message);

    protected void sendMessage(String text) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setText(text);
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(String text, String parseMode) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setParseMode(parseMode);
        textMessage.setText(text);
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(TextMessage textMessage) {
        Message sent = telegramClient.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void sendConfirmationReaction(Message message) {
        ReactionMessage reactionMessage = new ReactionMessage();
        reactionMessage.setChatId(message.getChatId());
        reactionMessage.setMessageId(message.getMessageId());
        List<ReactionType> reactionTypes = new ArrayList<>();
        reactionTypes.add(new ReactionTypeEmoji(ReactionType.EMOJI_TYPE, "👾"));
        reactionMessage.setReactionTypes(reactionTypes);
        telegramClient.sendReaction(reactionMessage);
    }

    private void logInteraction() {
        logger.info("Command: [{}]: {}",
                message.getFrom().getUserName(),
                message.getText());
    }

    private void scheduleMessageToDelete(Message message) {
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

