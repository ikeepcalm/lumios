package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.telegram.campus.CampusApiClient;
import dev.ua.ikeepcalm.lumios.telegram.campus.CampusAuthException;
import dev.ua.ikeepcalm.lumios.telegram.campus.CampusSubscriptionResult;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.interactions.commands.campus.LinkCommand;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@BotUpdate
@Component
public class CampusLinkUpdate extends ServicesShortcut implements Interaction {

    /**
     * Tracks users awaiting credential input.
     * Key: Telegram user ID, Value: timestamp when the pending session was created.
     * Entries expire automatically after 5 minutes.
     */
    public static final Cache<Long, Long> pendingLinks = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final long PENDING_SESSION_TTL_MS = TimeUnit.MINUTES.toMillis(5);

    @Value("${campus.webhook.url:}")
    private String webhookUrl;

    private final CampusApiClient campusApiClient;
    private final CampusBindingService campusBindingService;

    public CampusLinkUpdate(CampusApiClient campusApiClient, CampusBindingService campusBindingService) {
        this.campusApiClient = campusApiClient;
        this.campusBindingService = campusBindingService;
    }

    @Override
    public void fireInteraction(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message message = update.getMessage();

        if (!message.getChat().getType().equals("private")) {
            return;
        }

        long userId = message.getFrom().getId();

        Long sessionStart = pendingLinks.getIfPresent(userId);
        if (sessionStart == null) {
            return;
        }

        // Guard against stale sessions that slipped through before Caffeine eviction
        if (System.currentTimeMillis() - sessionStart > PENDING_SESSION_TTL_MS) {
            pendingLinks.invalidate(userId);
            return;
        }

        pendingLinks.invalidate(userId);

        // Delete the credentials message immediately before doing anything else
        try {
            telegramClient.sendRemoveMessage(new RemoveMessage(message.getMessageId(), message.getChatId()));
        } catch (TelegramApiException e) {
            log.warn("Could not delete credentials message for userId={}: {}", userId, e.getMessage());
        }

        String text = message.getText().trim();
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            sendDirectMessage(message.getChatId(), "Невірний формат. Очікується: `логін пароль`. Використай /link, щоб спробувати знову.");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("campus.webhook.url is not configured — cannot subscribe userId={}", userId);
            sendDirectMessage(message.getChatId(), "Сервіс тимчасово недоступний. Спробуй пізніше.");
            return;
        }

        incrementRateLimitAttempt(userId);

        CampusSubscriptionResult result;
        try {
            result = campusApiClient.subscribe(username, password, webhookUrl, String.valueOf(userId));
        } catch (CampusAuthException e) {
            log.warn("Campus subscription failed for userId={}: {}", userId, e.getMessage());
            sendDirectMessage(message.getChatId(), "Не вдалося підключитися до eCampus. Перевір логін та пароль і спробуй знову через /link.");
            return;
        } finally {
            // Null out references so credentials become eligible for GC immediately
            username = null;
            password = null;
        }

        CampusBinding binding = new CampusBinding();
        binding.setTelegramUserId(userId);
        binding.setAccessToken(result.getAccessToken());
        binding.setSubscribedAt(LocalDateTime.now());
        campusBindingService.save(binding);

        sendDirectMessage(message.getChatId(), """
                Аккаунт успішно прив'язано до eCampus!

                Відтепер ти будеш отримувати сповіщення про нові оцінки прямо в цей чат.
                Щоб відв'язати аккаунт, використай /unlink.
                """);
    }

    /** Sends a message directly to a chat without replying to any deleted message. */
    private void sendDirectMessage(long chatId, String text) {
        TextMessage msg = new TextMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        telegramClient.sendTextMessage(msg);
    }

    private void incrementRateLimitAttempt(long userId) {
        AtomicInteger attempts = LinkCommand.getRateLimitCache().get(userId, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
    }

}
