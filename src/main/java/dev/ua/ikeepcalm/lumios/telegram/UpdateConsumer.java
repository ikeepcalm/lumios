package dev.ua.ikeepcalm.lumios.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.*;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateConsumer.class);
    private static final String BOT_USERNAME = "@lumios_bot";
    //    private static final String BOT_USERNAME = "@localios_bot";
    private static final long RATE_LIMIT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);
    private static final int MAX_REQUESTS_PER_INTERVAL = 5;

    private final Cache<Long, UserActivity> userActivityCache;
    public static final HashMap<Long, Long> waitingTasks = new HashMap<>();
    public static final HashMap<Long, Long> waitingLinks = new HashMap<>();

    private final List<Interaction> commandHandlers;
    private final List<Interaction> channelHandlers;
    private final List<Interaction> callbackHandlers;
    private final List<Interaction> updateHandlers;
    private final List<Interaction> reactionHandlers;
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
        this.commandHandlers = getBeansWithAnnotation(context, BotCommand.class);
        this.channelHandlers = getBeansWithAnnotation(context, BotChannel.class);
        this.callbackHandlers = getBeansWithAnnotation(context, BotCallback.class);
        this.updateHandlers = getBeansWithAnnotation(context, BotUpdate.class);
        this.reactionHandlers = getBeansWithAnnotation(context, BotReaction.class);
    }

    @Override
    public void consume(Update update) {
        if (rateLimit(update)) {
            return;
        }

        InteractionType interactionType = determineInteractionType(update);
        if (interactionType == null) {
            log.warn("Unknown interaction type{}", update);
            return;
        }

        switch (interactionType) {
            case COMMAND -> handleInteraction(update, commandHandlers, BotCommand.class);
            case CALLBACK -> handleInteraction(update, callbackHandlers, BotCallback.class);
            case INLINE_QUERY -> handleInlineQuery(update);
            case UPDATE -> handleUpdate(update);
            case CHANNEL -> handleChannel(update);
            case REACTION_PLUS -> handleReaction(update, true);
            case REACTION_MINUS -> handleReaction(update, false);
        }
    }

    private <T extends java.lang.annotation.Annotation> List<Interaction> getBeansWithAnnotation(ApplicationContext context, Class<T> annotationClass) {
        return context.getBeansWithAnnotation(annotationClass)
                .values().stream()
                .map(Interaction.class::cast)
                .collect(Collectors.toList());
    }

    protected void handleInteraction(Update update, List<Interaction> handlers, Class<? extends java.lang.annotation.Annotation> annotationClass) {
        for (Interaction handler : handlers) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(handler);
            java.lang.annotation.Annotation annotation = targetClass.getAnnotation(annotationClass);
            if (annotation instanceof BotCommand && matchCommand(update.getMessage(), (BotCommand) annotation)) {
                LumiosChat chat = getOrCreateChat(update.getMessage().getChatId(), update.getMessage().getChat().getTitle());
                LumiosUser user = getOrCreateUser(update.getMessage().getFrom().getId(), chat, update.getMessage().getFrom().getUserName());
                handler.fireInteraction(update, user, chat);
                return;
            } else if (annotation instanceof BotCallback && matchCallback(update.getCallbackQuery(), (BotCallback) annotation)) {
                LumiosChat chat = getOrCreateChat(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getChat().getTitle());
                LumiosUser user = getOrCreateUser(update.getCallbackQuery().getFrom().getId(), chat, update.getCallbackQuery().getFrom().getUserName());
                handler.fireInteraction(update.getCallbackQuery(), user, chat);
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
        for (Interaction handler : updateHandlers) {
            handler.fireInteraction(update);
        }
    }

    private void handleChannel(Update update) {
        for (Interaction handler : channelHandlers) {
            handler.fireInteraction(update);
        }
    }

    private void handleReaction(Update update, boolean isPlus) {
        for (Interaction handler : reactionHandlers) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(handler);
            BotReaction annotation = targetClass.getAnnotation(BotReaction.class);
            if (!annotation.isPlus() && isPlus) {
                return;
            }

            LumiosChat chat = getOrCreateChat(update.getMessageReaction().getChat().getId(), update.getMessageReaction().getChat().getTitle());
            LumiosUser user = getOrCreateUser(update.getMessageReaction().getUser().getId(), chat, update.getMessageReaction().getUser().getUserName());

            handler.fireInteraction(update, user, chat);
        }
    }

    private LumiosChat getOrCreateChat(Long chatId, String chatTitle) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            chat = new LumiosChat();
            chat.setChatId(chatId);
            chat.setName(chatTitle);
            chat.setTimetableEnabled(true);
            chatService.save(chat);
        }
        return chat;
    }

    private LumiosUser getOrCreateUser(Long userId, LumiosChat chat, String username) {
        LumiosUser user;
        try {
            user = userService.findById(userId, chat);
        } catch (NoSuchEntityException e) {
            user = new LumiosUser();
            user.setChat(chat);
            user.setUserId(userId);
            user.setUsername(username);
            user.setSustainable(100);
            user.setCredits(100);
            userService.save(user);
        }
        return user;
    }

    private boolean matchCommand(Message message, BotCommand annotation) {
        String text = message.getText().split(" ")[0].replace(BOT_USERNAME, "").replace("/", "");
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

    private boolean matchCallback(CallbackQuery callbackQuery, BotCallback annotation) {
        if (annotation.callback().isEmpty()) {
            if (annotation.startsWith().isEmpty()) {
                return callbackQuery.getData().endsWith(annotation.endsWith());
            }
            return callbackQuery.getData().startsWith(annotation.startsWith());
        }
        return callbackQuery.getData().equals(annotation.callback());
    }

    private boolean rateLimit(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getFrom().getId();
            long currentTime = System.currentTimeMillis();

            UserActivity userActivity = userActivityCache.get(userId, k -> new UserActivity());
            if (Objects.requireNonNull(userActivity).isRateLimited(currentTime)) {
                log.info("User {} is rate limited", userId);
                return true;
            }

            userActivity.recordRequest(currentTime);
            userActivityCache.put(userId, userActivity);
        }
        return false;
    }

    private InteractionType determineInteractionType(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                log.info("Command: {}", update.getMessage().getText());
                return InteractionType.COMMAND;
            }
            return InteractionType.UPDATE;
        }
        if (update.hasChannelPost()) {
            log.info("Channel post: {}", update.getChannelPost().getText());
            return InteractionType.CHANNEL;
        }
        if (update.hasCallbackQuery()) {
            log.info("Callback: {}", update.getCallbackQuery().getData());
            return InteractionType.CALLBACK;
        }
        if (update.hasInlineQuery()) {
            log.info("Inline query: {}", update.getInlineQuery().getQuery());
            return InteractionType.INLINE_QUERY;
        }
        if (update.getMessageReaction() != null) {
            MessageReactionUpdated reactionUpdated = update.getMessageReaction();
            int oldCount = reactionUpdated.getOldReaction().size();
            int newCount = reactionUpdated.getNewReaction().size();
            if (oldCount < newCount) {
                return InteractionType.REACTION_PLUS;
            } else if (oldCount > newCount) {
                return InteractionType.REACTION_MINUS;
            }
        }
        return null;
    }

    private static class UserActivity {
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
        UPDATE,
        CHANNEL,
        REACTION_PLUS,
        REACTION_MINUS
    }
}
