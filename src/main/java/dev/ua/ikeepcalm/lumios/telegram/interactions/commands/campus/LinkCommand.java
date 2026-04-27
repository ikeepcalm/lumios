package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.campus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.interactions.updates.CampusLinkUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.methods.ParseMode;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@BotCommand(command = "link")
public class LinkCommand extends ServicesShortcut implements Interaction {

    private static final int MAX_ATTEMPTS = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Cache<Long, AtomicInteger> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    private final CampusBindingService campusBindingService;

    public LinkCommand(CampusBindingService campusBindingService) {
        this.campusBindingService = campusBindingService;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();

        if (!message.getChat().getType().equals("private")) {
            sendMessage("🔒 Команда /link доступна лише в приватному чаті з ботом.", message);
            return;
        }

        long userId = message.getFrom().getId();

        try {
            CampusBinding existing = campusBindingService.findByTelegramUserId(userId);
            if (existing.getExternalId() == null) {
                existing.setExternalId(UUID.randomUUID().toString());
                campusBindingService.save(existing);
            }
            String idLine = "`" + existing.getExternalId() + "`";
            sendMessage("""
                    ✅ *Акаунт вже прив'язано до eCampus*

                    📅 Прив'язано: %s
                    🆔 ID підписки: %s

                    Щоб відв'язати і підключити інший — використай /unlink.
                    """.formatted(existing.getSubscribedAt().format(DATE_FORMATTER), idLine),
                    ParseMode.MARKDOWN, message);
            return;
        } catch (NoSuchEntityException ignored) {
        }

        AtomicInteger attempts = rateLimitCache.get(userId, k -> new AtomicInteger(0));
        if (attempts.get() >= MAX_ATTEMPTS) {
            sendMessage("⏳ Забагато спроб прив'язки. Спробуй знову через 30 хвилин.", message);
            return;
        }

        CampusLinkUpdate.pendingLinks.put(userId, System.currentTimeMillis());

        sendMessage("""
                🔗 *Прив'язка акаунту eCampus*

                Надішли свої дані для входу у форматі:
                `логін пароль`

                Наприклад: `ivan.petrenko mypassword123`

                🔐 Повідомлення з паролем буде негайно видалено.
                Дані не зберігаються на сервері.
                ⏱ У тебе є 5 хвилин, після чого сесія скидається.
                """, ParseMode.MARKDOWN, message);
    }

    public static Cache<Long, AtomicInteger> getRateLimitCache() {
        return rateLimitCache;
    }

}
