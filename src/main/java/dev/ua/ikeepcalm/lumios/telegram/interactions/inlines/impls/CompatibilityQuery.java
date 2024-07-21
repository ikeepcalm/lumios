package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.impls;

import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotInlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

import java.util.Random;

@Component
@BotInlineQuery(inlineQuery = "Наскільки ти сумісний із <твоя річ> ?")
public class CompatibilityQuery implements InlineQuery {

    private final String[] emojis = {"😊", "🚀", "🌟", "🎉", "👾", "💻", "📚", "🎨"};
    private final String[] strings = {"рижою мавпою", "мовою програмування Java", "успішним життям", "спекою 40 градусів", "піцою з ананасом", "програмою Microsoft Excel"};

    public InlineQueryResult processUpdate(Update update) {
        String query = update.getInlineQuery().getQuery();
        if (query.isBlank()) {
            String randomString = getRandomString();
            return InlineQueryResultArticle.builder()
                    .id("compatibility")
                    .title("Наскільки ти сумісний із " + randomString + "?")
                    .description("Дізнайтеся, на скільки відсотків ви сумісні з вашою річчю (або людиною, хі-хі)!")
                    .inputMessageContent(InputTextMessageContent.builder()
                            .messageText("Ви із " + randomString + " підходите один одному на " + (new Random().nextInt(100) + 1) + "% " + getRandomEmoji())
                            .build())
                    .build();
        } else {
            return InlineQueryResultArticle.builder()
                    .id("compatibility")
                    .thumbnailUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5UECHdmuFUHHT7Nc_1mty8G6T1SUiAfKcbw&s")
                    .title("Наскільки ти сумісний із " + query + "?")
                    .description("Дізнайтеся, на скільки відсотків ви сумісні з вашою річчю (або людиною, хі-хі)!")
                    .inputMessageContent(InputTextMessageContent.builder()
                            .messageText("Ви із " + query + " підходите один одному на " + (new Random().nextInt(100) + 1) + "% " + getRandomEmoji())
                            .build())
                    .build();
        }
    }

    private String getRandomEmoji() {
        Random random = new Random();
        return emojis[random.nextInt(emojis.length)];
    }

    private String getRandomString() {
        Random random = new Random();
        return strings[random.nextInt(strings.length)];
    }

}
