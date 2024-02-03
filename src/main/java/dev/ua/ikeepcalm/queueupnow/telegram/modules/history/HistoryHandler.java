package dev.ua.ikeepcalm.queueupnow.telegram.modules.history;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.history.updates.MediaUpdate;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.history.updates.TextUpdate;
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
            textUpdate.processUpdate(update);
        } else {
            mediaUpdate.processUpdate(update);
        }
    }

    @Override
    public boolean supports(Update update) {
        return update.hasMessage();
    }
}
