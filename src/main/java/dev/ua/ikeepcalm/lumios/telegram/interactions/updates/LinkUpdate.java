package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.UpdateConsumer;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@BotUpdate
public class LinkUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update) {
        long userId = update.getMessage().getFrom().getId();
        if (UpdateConsumer.waitingLinks.containsKey(userId)) {
            ClassEntry entry;
            try {
                entry = timetableService.findClassById(UpdateConsumer.waitingLinks.get(userId));
            } catch (NoSuchEntityException e) {
                return;
            }

            dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat chat = null;
            try {
                chat = chatService.findByChatId(update.getMessage().getChatId());
            } catch (NoSuchEntityException ignored) {
            }

            if (isValidUrl(update.getMessage().getText())) {
                entry.setUrl(update.getMessage().getText());
                UpdateConsumer.waitingLinks.remove(userId);
                timetableService.save(entry);

                sendMessage(translationService.getMessage("class.link.add.success", chat), update.getMessage());
            } else {
                UpdateConsumer.waitingLinks.remove(userId);
                sendMessage(translationService.getMessage("class.link.add.invalid", chat), update.getMessage());
            }
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
