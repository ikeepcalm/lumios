package dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Transactional
public interface Interaction {

    default void fireInteraction(Update update) {
        throw new NotImplementedException("Method not implemented");
    }

    default void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        throw new NotImplementedException("Method not implemented");
    }

    default void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        throw new NotImplementedException("Method not implemented");
    }

}
