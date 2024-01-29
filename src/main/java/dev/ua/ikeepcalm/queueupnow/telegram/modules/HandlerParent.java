package dev.ua.ikeepcalm.queueupnow.telegram.modules;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface HandlerParent {
    void dispatchUpdate(Update update);
    default boolean supports(Update update) {
        if (update != null) {
            if (update.getMessage() != null ) {
                if (update.getMessage().hasText() && !update.getMessage().getText().isEmpty()) {
                    return update.getMessage().getText().startsWith("/");
                } else {
                    return false;
                }
            } else if (update.hasCallbackQuery()) {
                return true;
            } else {
                return update.getMessageReaction() != null;
            }
        } else {
            return false;
        }
    }
}

