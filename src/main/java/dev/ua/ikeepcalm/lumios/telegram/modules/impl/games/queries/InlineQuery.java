package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.queries;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

public interface InlineQuery {

    InlineQueryResult processUpdate(Update update);

}
