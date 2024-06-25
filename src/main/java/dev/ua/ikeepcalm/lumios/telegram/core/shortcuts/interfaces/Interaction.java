package dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Interaction {

    default void fireInteraction(Update message, LumiosUser user, LumiosChat chat) {
        throw new NotImplementedException("Method not implemented");
    }

    default void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat){
        throw new NotImplementedException("Method not implemented");
    };

}
