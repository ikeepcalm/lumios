package dev.ua.ikeepcalm.queueupnow.telegram.handling.handlers;

import dev.ua.ikeepcalm.queueupnow.telegram.executing.callbacks.ExitCallback;
import dev.ua.ikeepcalm.queueupnow.telegram.executing.callbacks.FlushCallback;
import dev.ua.ikeepcalm.queueupnow.telegram.executing.callbacks.JoinCallback;
import dev.ua.ikeepcalm.queueupnow.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CallbackHandler implements Handleable {

    @Autowired
    private JoinCallback joinCallback;

    @Autowired
    private FlushCallback flushCallback;

    @Autowired
    private ExitCallback exitCallback;

    @Override
    public void manage(Update update) {
        String callback = update.getCallbackQuery().getData();
        CallbackQuery origin = update.getCallbackQuery();
        if (callback.endsWith("-join")){
            callback = callback.replace("-join", "");
            joinCallback.manage(callback, origin);
        } else if (callback.endsWith("-flush")){
            callback = callback.replace("-flush", "");
            flushCallback.manage(callback, origin);
        } else if (callback.endsWith("-exit")){
            callback = callback.replace("-exit", "");
            exitCallback.manage(callback, origin);
        }
    }

    @Override
    public boolean supports(Update update) {
        return update.getCallbackQuery() != null;
    }
}
