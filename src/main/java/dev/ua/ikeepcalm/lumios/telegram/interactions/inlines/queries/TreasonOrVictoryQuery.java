package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.queries;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotInlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

@Component
@BotInlineQuery(inlineQuery = "Зрада чи перемога?")
public class TreasonOrVictoryQuery implements InlineQuery {

    public InlineQueryResult processUpdate(Update update) {
        String[] options = {
                "Це повна зрада!",
                "Невже перемога? Перемога буде, люди!",
                "Боротьба триває!",
                "Ганебна зрада!",
                "Перемога наша!",
                "Зрада, зрада, зрада!",
                "Перемога наша, браття!",
                "Зрада, руки опускаються...",
                "Пес Патрон, Пес Патрон, таких нам треба батальйон!",
                ""};
        return InlineQueryResultArticle.builder()
                .id("treason_or_victory")
                .thumbnailUrl("https://mil.co.ua/wp-content/uploads/2023/06/27_main_9_500x317.jpg")
                .title("Зрада чи перемога?")
                .description("Дізнайтеся, чи на вас сьогодні чекає перемога чи зрада")
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(options[(int) (Math.random() * options.length)])
                        .build())
                .build();
    }

}
