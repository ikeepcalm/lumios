package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

public interface InlineQuery {

    InlineQueryResult processUpdate(Update update);

}
