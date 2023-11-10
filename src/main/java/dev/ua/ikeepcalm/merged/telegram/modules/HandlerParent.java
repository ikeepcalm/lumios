package dev.ua.ikeepcalm.merged.telegram.modules;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface HandlerParent {
    void dispatchUpdate(Update update);
    default boolean supports(Update update) {
        if (update != null) {
            if (update.hasMessage() && update.getMessage() != null) {
                if (update.getMessage().hasText() && !update.getMessage().getText().isEmpty()) {
                    return update.getMessage().getText().startsWith("/");
                } else {
                    return false;
                }
            } else {
                return update.hasCallbackQuery();
            }
        } else {
            return false;
        }
    }
}

