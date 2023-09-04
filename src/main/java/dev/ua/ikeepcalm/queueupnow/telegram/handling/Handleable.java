package dev.ua.ikeepcalm.queueupnow.telegram.handling;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Handleable {
    void manage(Update update);
    boolean supports(Update update);
}
