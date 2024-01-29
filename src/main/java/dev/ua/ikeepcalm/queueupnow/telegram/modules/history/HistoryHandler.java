package dev.ua.ikeepcalm.queueupnow.telegram.modules.history;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.history.updates.TextUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class HistoryHandler implements HandlerParent {

    private final TextUpdate textUpdate;

    public HistoryHandler(TextUpdate textUpdate) {
        this.textUpdate = textUpdate;
    }

    @Override
    public void dispatchUpdate(Update update) {
        textUpdate.processUpdate(update);
    }

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
}
