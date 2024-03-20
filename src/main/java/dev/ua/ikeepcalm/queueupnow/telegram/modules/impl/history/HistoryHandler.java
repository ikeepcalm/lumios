package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.history;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.history.updates.MediaUpdate;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.history.updates.TextUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class HistoryHandler implements HandlerParent {

    private final TextUpdate textUpdate;
    private final MediaUpdate mediaUpdate;

    public HistoryHandler(TextUpdate textUpdate, MediaUpdate mediaUpdate) {
        this.textUpdate = textUpdate;
        this.mediaUpdate = mediaUpdate;
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.getMessage().hasText()){
            textUpdate.handleUpdate(update);
        } else {
            mediaUpdate.handleUpdate(update);
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.getMessage().getChat().getType().equals("private")) {
            return false;
        }

        return update.hasMessage();
    }
}
