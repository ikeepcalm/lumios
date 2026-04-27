package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.campus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.interactions.updates.CampusLinkUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@BotCommand(command = "link")
public class LinkCommand extends ServicesShortcut implements Interaction {

    /**
     * Rate limit: max attempts per user within the tracking window.
     * Stored as AtomicInteger so concurrent bot threads can update safely.
     */
    private static final int MAX_ATTEMPTS = 3;
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
            sendMessage("Команда /link доступна лише в приватних повідомленнях з ботом.", message);
            return;
        }

        long userId = message.getFrom().getId();

        if (campusBindingService.existsByTelegramUserId(userId)) {
            sendMessage("""
                    Твій аккаунт вже прив'язано до eCampus.

                    Використай /unlink, щоб відв'язати його, після чого зможеш прив'язати інший аккаунт.
                    """, message);
            return;
        }

        AtomicInteger attempts = rateLimitCache.get(userId, k -> new AtomicInteger(0));
        if (attempts.get() >= MAX_ATTEMPTS) {
            sendMessage("Забагато спроб прив'язки. Спробуй знову через 30 хвилин.", message);
            return;
        }

        CampusLinkUpdate.pendingLinks.put(userId, System.currentTimeMillis());

        sendMessage("""
                Для прив'язки аккаунту надішли мені свої дані для входу в eCampus у форматі:

                `логін пароль`

                Наприклад: `ivan.petrenko mypassword123`

                Повідомлення з паролем буде негайно видалено. Дані не зберігаються на сервері.
                У тебе є 5 хвилин, після чого потрібно буде починати спочатку.
                """, message);
    }

    public static Cache<Long, AtomicInteger> getRateLimitCache() {
        return rateLimitCache;
    }

}
