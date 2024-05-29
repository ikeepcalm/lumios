package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games;

import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.queries.InlineQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

import java.util.List;

@Component
public class InlineHandler implements HandlerParent {

    private static final Logger log = LoggerFactory.getLogger(InlineHandler.class);
    private final TelegramClient telegramClient;
    private final List<InlineQuery> inlineQueryList;

    public InlineHandler(TelegramClient telegramClient, List<InlineQuery> inlineQueryList) {
        this.telegramClient = telegramClient;
        this.inlineQueryList = inlineQueryList;
    }

    @Override
    public void dispatchUpdate(Update update) {
        List<InlineQueryResult> inlineQueryResults = inlineQueryList.stream().map(inlineQuery -> inlineQuery.processUpdate(update)).toList();
        AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder()
                .inlineQueryId(update.getInlineQuery().getId())
                .results(inlineQueryResults)
                .isPersonal(true)
                .cacheTime(0)
                .build();
        telegramClient.sendAnswerInlineQuery(answerInlineQuery);
        if (update.getInlineQuery().getQuery().length() < 5) {
            log.info("Empty query from user: {}", update.getInlineQuery().getFrom().getUserName());
        }
        log.info("Query: {}, User: {}", update.getInlineQuery().getQuery(), update.getInlineQuery().getFrom().getUserName());
    }

    @Override
    public boolean supports(Update update) {
        return update.hasInlineQuery();
    }

}
