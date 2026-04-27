package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.campus;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.campus.CampusApiClient;
import dev.ua.ikeepcalm.lumios.telegram.campus.CampusAuthException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@BotCommand(command = "unlink")
public class UnlinkCommand extends ServicesShortcut implements Interaction {

    private final CampusBindingService campusBindingService;
    private final CampusApiClient campusApiClient;

    public UnlinkCommand(CampusBindingService campusBindingService, CampusApiClient campusApiClient) {
        this.campusBindingService = campusBindingService;
        this.campusApiClient = campusApiClient;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();

        if (!message.getChat().getType().equals("private")) {
            sendMessage("🔒 Команда /unlink доступна лише в приватному чаті з ботом.", message);
            return;
        }

        long userId = message.getFrom().getId();

        CampusBinding binding;
        try {
            binding = campusBindingService.findByTelegramUserId(userId);
        } catch (NoSuchEntityException e) {
            sendMessage("ℹ️ Твій акаунт не прив'язано до eCampus. Використай /link для прив'язки.", message);
            return;
        }

        try {
            campusApiClient.unsubscribe(binding.getAccessToken());
        } catch (CampusAuthException e) {
            log.warn("Could not revoke campus subscription for userId={}: {}", userId, e.getMessage());
        }

        campusBindingService.deleteByTelegramUserId(userId);
        sendMessage("✅ Акаунт успішно відв'язано від eCampus. Сповіщення про оцінки більше не надходитимуть.", message);
    }

}
