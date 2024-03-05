package dev.ua.ikeepcalm.queueupnow.telegram;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final List<HandlerParent> handlerParentList;

    @Override
    public void consume(Update update) {
        for (HandlerParent handlerParent : this.handlerParentList) {
            if (handlerParent.supports(update)){
                handlerParent.dispatchUpdate(update);
            }
        }
    }

    public UpdateConsumer(List<HandlerParent> handlerParentList) {
        this.handlerParentList = handlerParentList;
    }

}

