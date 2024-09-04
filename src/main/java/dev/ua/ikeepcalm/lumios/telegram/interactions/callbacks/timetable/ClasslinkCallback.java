package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.UpdateConsumer;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@BotCallback(startsWith = "classlink-add-")
public class ClasslinkCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] data = callbackQuery.getData().split("-");
        long classId = Long.parseLong(data[2]);
        try {
            ClassEntry classEntry = timetableService.findClassById(classId);
            if (classEntry.getUrl() != null) {
                telegramClient.sendAnswerCallbackQuery("Посилання вже додано до класу", callbackQuery.getId());
                return;
            }

            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(chat.getChatId());
            editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessage.setText("@" + callbackQuery.getFrom().getUserName() + ", надішліть посилання на конференцію у наступному повідомленні: ");
            editMessage(editMessage);

            UpdateConsumer.waitingLinks.put(callbackQuery.getFrom().getId(), classId);
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Клас не знайдено. Схоже на серверну помилку, зверніться до підтримки!", callbackQuery.getId());
        }
    }
}
