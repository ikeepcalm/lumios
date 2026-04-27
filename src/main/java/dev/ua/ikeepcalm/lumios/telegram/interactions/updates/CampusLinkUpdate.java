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
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@BotUpdate
@Component
public class CampusLinkUpdate extends ServicesShortcut implements Interaction {

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

        if (System.currentTimeMillis() - sessionStart > PENDING_SESSION_TTL_MS) {
            pendingLinks.invalidate(userId);
            return;
        }

        pendingLinks.invalidate(userId);

        try {
            telegramClient.sendRemoveMessage(new RemoveMessage(message.getMessageId(), message.getChatId()));
        } catch (TelegramApiException e) {
            log.warn("Could not delete credentials message for userId={}: {}", userId, e.getMessage());
        }

        String text = message.getText().trim();
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            sendDirectMessage(message.getChatId(), "❌ Невірний формат. Очікується: `логін пароль`\n\nСпробуй знову через /link.", ParseMode.MARKDOWN);
            return;
        }

        String username = parts[0];
        String password = parts[1];

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("campus.webhook.url is not configured — cannot subscribe userId={}", userId);
            sendDirectMessage(message.getChatId(), "⚙️ Сервіс тимчасово недоступний. Спробуй пізніше.", null);
            return;
        }

        incrementRateLimitAttempt(userId);

        String externalId = UUID.randomUUID().toString();

        CampusSubscriptionResult result;
        try {
            result = campusApiClient.subscribe(username, password, webhookUrl, externalId);
        } catch (CampusAuthException e) {
            log.warn("Campus subscription failed for userId={}: {}", userId, e.getMessage());
            sendDirectMessage(message.getChatId(),
                    "❌ Не вдалося підключитися до eCampus.\n\nПеревір логін та пароль і спробуй знову через /link.", null);
            return;
        } finally {
            username = null;
            password = null;
        }

        CampusBinding binding = new CampusBinding();
        binding.setTelegramUserId(userId);
        binding.setExternalId(externalId);
        binding.setAccessToken(result.getAccessToken());
        binding.setSubscribedAt(LocalDateTime.now());
        campusBindingService.save(binding);

        sendDirectMessage(message.getChatId(), """
                ✅ *Акаунт успішно прив'язано до eCampus!*

                Відтепер ти отримуватимеш сповіщення про нові оцінки прямо сюди.
                Щоб відв'язати акаунт — використай /unlink.
                """, ParseMode.MARKDOWN);
    }

    private void sendDirectMessage(long chatId, String text, String parseMode) {
        TextMessage msg = new TextMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode(parseMode);
        telegramClient.sendTextMessage(msg);
    }

    private void incrementRateLimitAttempt(long userId) {
        AtomicInteger attempts = LinkCommand.getRateLimitCache().get(userId, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
    }

}
