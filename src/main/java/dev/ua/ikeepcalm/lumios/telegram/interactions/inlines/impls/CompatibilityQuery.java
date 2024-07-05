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

    public InlineQueryResult processUpdate(Update update) {
        String query = update.getInlineQuery().getQuery();

        return InlineQueryResultArticle.builder()
                .id("compatibility")
                .thumbnailUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5UECHdmuFUHHT7Nc_1mty8G6T1SUiAfKcbw&s")
                .title("Наскільки ти сумісний із <твоя річ> ?")
                .description("Дізнайтеся, на скільки відсотків ви сумісні з вашою річчю (або людиною, хі-хі)!")
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText("Ви із " + query + " підходите один одному на " + (new Random().nextInt(100) + 1) + "% " + getRandomEmoji())
                        .build())
                .build();
    }

    public String getRandomEmoji() {
        Random random = new Random();
        return emojis[random.nextInt(emojis.length)];
    }

}
