package dev.ua.ikeepcalm.queueupnow.telegram.modules;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.*;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.AbsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import java.util.List;
import java.util.Objects;

@Component
public abstract class UpdateParent {
    protected AbsSender absSender;
    protected ReverenceChat reverenceChat;
    protected ReverenceUser reverenceUser;
    protected ChatService chatService;
    protected UserService userService;
    protected TaskService taskService;
    protected ShopService shopService;
    protected RecordService recordService;
    protected TimetableService timetableService;
    protected QueueService queueService;

    @Autowired
    private void setupDependencies(AbsSender absSender,
                                   ChatService chatService,
                                   UserService userService,
                                   TaskService taskService,
                                   ShopService shopService,
                                   RecordService recordService,
                                   TimetableService timetableService,
                                   QueueService queueService) {
        this.absSender = absSender;
        this.chatService = chatService;
        this.userService = userService;
        this.taskService = taskService;
        this.shopService = shopService;
        this.timetableService = timetableService;
        this.queueService = queueService;
        this.recordService = recordService;
    }

    protected void instantiateUpdate(Update update) {
        if (update.getMessageReaction() != null) {
            MessageReactionUpdated message = update.getMessageReaction();
            try {
                this.reverenceChat = chatService.findByChatId(message.getChat().getId());
            } catch (NoSuchEntityException e) {
                ReverenceChat newChat = new ReverenceChat();
                newChat.setChatId(message.getChat().getId());
                this.chatService.save(newChat);
                this.reverenceChat = newChat;
            }

            if (!message.getUser().getIsBot()) {
                try {
                    this.reverenceUser = this.userService.findById(message.getUser().getId(), reverenceChat);
                    if (!Objects.equals(reverenceUser.getUsername(), message.getUser().getUserName())) {
                        reverenceUser.setUsername(message.getUser().getUserName());
                        userService.save(reverenceUser);
                    }
                } catch (NoSuchEntityException e) {
                    ReverenceUser newUser = new ReverenceUser();
                    newUser.setUserId(message.getUser().getId());
                    newUser.setUsername(message.getUser().getUserName());
                    newUser.setCredits(100);
                    newUser.setSustainable(100);
                    newUser.setChannel(reverenceChat);
                    userService.save(newUser);
                    reverenceUser = newUser;
                }
            }
        } else {
            Message message = update.getMessage();
            try {
                this.reverenceChat = chatService.findByChatId(update.getMessage().getChat().getId());
            } catch (NoSuchEntityException e) {
                ReverenceChat newChat = new ReverenceChat();
                newChat.setChatId(update.getMessage().getChat().getId());
                this.chatService.save(newChat);
                this.reverenceChat = newChat;
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
                    newUser.setChannel(reverenceChat);
                    userService.save(newUser);
                    reverenceUser = newUser;
                }
            }
        }
    }

    public abstract void processUpdate(Update update);

    protected ReverenceReaction findNewReaction(List<ReactionType> oldList, List<ReactionType> newList) {
        ReverenceReaction newReaction = ReverenceReaction.DEFAULT;

        for (ReactionType iteReaction : newList) {
            if (!oldList.contains(iteReaction)) {
                if (iteReaction instanceof ReactionTypeEmoji){
                    newReaction = ReverenceReaction.determineReaction(((ReactionTypeEmoji) iteReaction).getEmoji());
                }
            }
        }
        return newReaction;
    }

}

