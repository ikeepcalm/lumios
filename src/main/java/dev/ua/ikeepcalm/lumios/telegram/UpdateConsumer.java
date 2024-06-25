package dev.ua.ikeepcalm.lumios.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateConsumer.class);
    private final String BOT_USERNAME = "@lumios_bot";

    private final Cache<Long, UserActivity> userActivityCache;

    private final List<Interaction> commandHandlers;
    private final List<Interaction> callbackHandlers;
    private final List<Interaction> updateHandlers;

    private final List<InlineQuery> inlineQueryList;

    private final UserService userService;
    private final ChatService chatService;
    private final TelegramClient telegramClient;

    @Autowired
    public UpdateConsumer(ApplicationContext context, List<InlineQuery> inlineQueryList, UserService userService, ChatService chatService, TelegramClient telegramClient) {
        this.inlineQueryList = inlineQueryList;
        this.userService = userService;
        this.chatService = chatService;
        this.telegramClient = telegramClient;
        this.userActivityCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
        this.commandHandlers = context.getBeansWithAnnotation(BotCommand.class)
                .values().stream()
                .map(Interaction.class::cast)
                .collect(Collectors.toList());
        this.callbackHandlers = context.getBeansWithAnnotation(BotCallback.class)
                .values().stream()
                .map(Interaction.class::cast)
                .collect(Collectors.toList());
        this.updateHandlers = context.getBeansWithAnnotation(BotUpdate.class)
                .values().stream()
                .map(Interaction.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void consume(Update update) {
        if (rateLimit(update)) {
            return;
        }

        InteractionType interactionType = determineInteractionType(update);
        switch (interactionType) {
            case COMMAND -> handleCommand(update);
            case CALLBACK -> handleCallback(update);
            case INLINE_QUERY -> handleInlineQuery(update);
            case UPDATE -> handleUpdate(update);
            case null -> log.warn("Unknown interaction type" + update);
        }
    }

    private void handleCommand(Update update) {
        Message message = update.getMessage();
        for (Interaction handler : commandHandlers) {
            BotCommand annotation = handler.getClass().getAnnotation(BotCommand.class);
            if (annotation != null && matchCommand(message, annotation)) {
                log.info("Matched command: {}", annotation.command());
                LumiosChat chat;
                try {
                    chat = chatService.findByChatId(message.getChatId());
                } catch (NoSuchEntityException e) {
                    LumiosChat newChat = new LumiosChat();
                    newChat.setChatId(message.getChatId());
                    newChat.setName(message.getChat().getTitle());
                    newChat.setTimetableEnabled(true);
                    chatService.save(newChat);
                    chat = newChat;
                }
                LumiosUser user;
                try {
                    user = userService.findById(message.getFrom().getId(), chat);
                } catch (NoSuchEntityException e) {
                    LumiosUser newUser = new LumiosUser();
                    newUser.setChat(chat);
                    newUser.setUserId(message.getFrom().getId());
                    newUser.setUsername(message.getFrom().getUserName());
                    newUser.setSustainable(100);
                    newUser.setCredits(100);
                    userService.save(newUser);
                    user = newUser;
                }
                handler.fireInteraction(update, user, chat);
                return;
            }
        }
    }

    private void handleInlineQuery(Update update) {
        List<InlineQueryResult> inlineQueryResults = inlineQueryList.stream().map(inlineQuery -> inlineQuery.processUpdate(update)).toList();
        AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder()
                .inlineQueryId(update.getInlineQuery().getId())
                .results(inlineQueryResults)
                .isPersonal(true)
                .cacheTime(0)
                .build();
        telegramClient.sendAnswerInlineQuery(answerInlineQuery);
    }

    private void handleUpdate(Update update) {
        Message message = update.getMessage();
        for (Interaction handler : updateHandlers) {
            LumiosChat chat;
            try {
                chat = chatService.findByChatId(message.getChatId());
            } catch (NoSuchEntityException e) {
                LumiosChat newChat = new LumiosChat();
                newChat.setChatId(message.getChatId());
                newChat.setName(message.getChat().getTitle());
                newChat.setTimetableEnabled(true);
                chatService.save(newChat);
                chat = newChat;
            }
            LumiosUser user;
            try {
                user = userService.findById(message.getFrom().getId(), chat);
            } catch (NoSuchEntityException e) {
                LumiosUser newUser = new LumiosUser();
                newUser.setChat(chat);
                newUser.setUserId(message.getFrom().getId());
                newUser.setUsername(message.getFrom().getUserName());
                newUser.setSustainable(100);
                newUser.setCredits(100);
                userService.save(newUser);
                user = newUser;
            }
            handler.fireInteraction(update, user, chat);
        }
    }

    private void handleCallback(Update update) {
        CallbackQuery message = update.getCallbackQuery();
        for (Interaction handler : callbackHandlers) {
            BotCallback annotation = handler.getClass().getAnnotation(BotCallback.class);

            if (annotation == null) {
                continue;
            }

            boolean match = false;

            if (annotation.callback().isEmpty()){
                match = message.getData().endsWith(annotation.endsWith());
            }

            if (match) {
                LumiosChat chat;
                try {
                    chat = chatService.findByChatId(message.getMessage().getChatId());
                } catch (NoSuchEntityException e) {
                    LumiosChat newChat = new LumiosChat();
                    newChat.setChatId(message.getMessage().getChatId());
                    newChat.setName(message.getMessage().getChat().getTitle());
                    newChat.setTimetableEnabled(true);
                    chatService.save(newChat);
                    chat = newChat;
                }
                LumiosUser user;
                try {
                    user = userService.findById(message.getFrom().getId(), chat);
                } catch (NoSuchEntityException e) {
                    LumiosUser newUser = new LumiosUser();
                    newUser.setChat(chat);
                    newUser.setUserId(message.getFrom().getId());
                    newUser.setUsername(message.getFrom().getUserName());
                    newUser.setSustainable(100);
                    newUser.setCredits(100);
                    userService.save(newUser);
                    user = newUser;
                }

                handler.fireInteraction(message, user, chat);
                return;
            }
        }
    }

    private boolean matchCommand(Message message, BotCommand annotation) {
        String text = message.getText();
        if (text.split(" ").length > 1) {
            text = text.split(" ")[0];
        }
        text = text.replace(BOT_USERNAME, "");
        text = text.replace("/", "");
        if (annotation.command().equals(text)) {
            return true;
        }
        if (!annotation.startsWith().isEmpty() && text.startsWith(annotation.startsWith())) {
            return true;
        }
        for (String alias : annotation.aliases()) {
            if (alias.equals(text)) {
                return true;
            }
        }
        return false;
    }

    private static class UserActivity {
        private final long RATE_LIMIT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);
        private final int MAX_REQUESTS_PER_INTERVAL = 5;
        private long lastRequestTime;
        private int requestCount;

        boolean isRateLimited(long currentTime) {
            if (currentTime - lastRequestTime > RATE_LIMIT_INTERVAL_MS) {
                lastRequestTime = currentTime;
                requestCount = 1;
                return false;
            } else {
                requestCount++;
                return requestCount > MAX_REQUESTS_PER_INTERVAL;
            }
        }

        void recordRequest(long currentTime) {
            if (currentTime - lastRequestTime > RATE_LIMIT_INTERVAL_MS) {
                lastRequestTime = currentTime;
                requestCount = 1;
            } else {
                requestCount++;
            }
        }
    }

    private enum InteractionType {
        COMMAND,
        CALLBACK,
        INLINE_QUERY,
        UPDATE
    }

    private InteractionType determineInteractionType(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                log.info("Command: {}", update.getMessage().getText());
                return InteractionType.COMMAND;
            }
            log.info("Update: {}", update.getMessage().getText());
            return InteractionType.UPDATE;
        }
        if (update.hasCallbackQuery()) {
            log.info("Callback: {}", update.getCallbackQuery().getData());
            return InteractionType.CALLBACK;
        }
        if (update.hasInlineQuery()) {
            log.info("Inline query: {}", update.getInlineQuery().getQuery());
            return InteractionType.INLINE_QUERY;
        }
        return null;
    }

    private boolean rateLimit(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getFrom().getId();
            long currentTime = System.currentTimeMillis();

            UserActivity userActivity = userActivityCache.get(userId, k -> new UserActivity());
            if (userActivity.isRateLimited(currentTime)) {
                log.info("User {} is rate limited", userId);
                return true;
            }

            userActivity.recordRequest(currentTime);
            userActivityCache.put(userId, userActivity);
        }
        return false;
    }
}
