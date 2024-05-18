package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.inlines;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

public interface InlineQuery {

    public InlineQueryResult processUpdate(Update update);

}
