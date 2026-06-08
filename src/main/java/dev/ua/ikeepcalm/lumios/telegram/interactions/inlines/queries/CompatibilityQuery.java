package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.queries;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotInlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

import java.util.Random;

@Component
@BotInlineQuery(inlineQuery = "Наскільки ти сумісний із <твоя річ> ?")
public class CompatibilityQuery implements InlineQuery {

    private final TranslationService translationService;
    private final String[] emojis = {"😊", "🚀", "🌟", "🎉", "👾", "💻", "📚", "🎨"};

    public CompatibilityQuery(TranslationService translationService) {
        this.translationService = translationService;
    }

    public InlineQueryResult processUpdate(Update update) {
        String lang = update.getInlineQuery().getFrom().getLanguageCode();
        String query = update.getInlineQuery().getQuery();
        if (query.isBlank()) {
            String randomString = getRandomString(lang);
            return InlineQueryResultArticle.builder()
                    .id("compatibility")
                    .title(translationService.getMessage("inline.compatibility.title", lang, randomString))
                    .description(translationService.getMessage("inline.compatibility.desc", lang))
                    .inputMessageContent(InputTextMessageContent.builder()
                            .messageText(translationService.getMessage("inline.compatibility.text", lang, randomString, (new Random().nextInt(100) + 1), getRandomEmoji()))
                            .build())
                    .build();
        } else {
            return InlineQueryResultArticle.builder()
                    .id("compatibility")
                    .thumbnailUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5UECHdmuFUHHT7Nc_1mty8G6T1SUiAfKcbw&s")
                    .title(translationService.getMessage("inline.compatibility.title", lang, query))
                    .description(translationService.getMessage("inline.compatibility.desc", lang))
                    .inputMessageContent(InputTextMessageContent.builder()
                            .messageText(translationService.getMessage("inline.compatibility.text", lang, query, (new Random().nextInt(100) + 1), getRandomEmoji()))
                            .build())
                    .build();
        }
    }

    private String getRandomEmoji() {
        Random random = new Random();
        return emojis[random.nextInt(emojis.length)];
    }

    private String getRandomString(String lang) {
        String thingsStr = translationService.getMessage("inline.compatibility.things", lang);
        String[] strings = thingsStr.split(",");
        Random random = new Random();
        return strings[random.nextInt(strings.length)];
    }

}
