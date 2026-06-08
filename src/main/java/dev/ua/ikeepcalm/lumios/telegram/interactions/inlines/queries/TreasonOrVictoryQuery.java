package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.queries;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotInlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

@Component
@BotInlineQuery(inlineQuery = "Зрада чи перемога?")
public class TreasonOrVictoryQuery implements InlineQuery {

    private final TranslationService translationService;

    public TreasonOrVictoryQuery(TranslationService translationService) {
        this.translationService = translationService;
    }

    public InlineQueryResult processUpdate(Update update) {
        String lang = update.getInlineQuery().getFrom().getLanguageCode();
        String optionsStr = translationService.getMessage("inline.treason.options", lang);
        String[] options = optionsStr.split(";");
        
        return InlineQueryResultArticle.builder()
                .id("treason_or_victory")
                .thumbnailUrl("https://mil.co.ua/wp-content/uploads/2023/06/27_main_9_500x317.jpg")
                .title(translationService.getMessage("inline.treason.title", lang))
                .description(translationService.getMessage("inline.treason.desc", lang))
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(options[(int) (Math.random() * options.length)])
                        .build())
                .build();
    }

}
