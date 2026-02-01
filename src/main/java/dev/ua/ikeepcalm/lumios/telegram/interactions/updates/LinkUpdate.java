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

            if (isValidUrl(update.getMessage().getText())) {
                entry.setUrl(update.getMessage().getText());
                UpdateConsumer.waitingLinks.remove(userId);
                timetableService.save(entry);

                sendMessage("✅ Посилання успішно додано!", update.getMessage());
            } else {
                UpdateConsumer.waitingLinks.remove(userId);
                sendMessage("❌ Невірний формат посилання. Ви були видалені зі списку очікування. Спробуйте ще раз, натиснувши на пару.", update.getMessage());
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
