package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.inlines.impls;

import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.inlines.InlineQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TruthOrDareQuery implements InlineQuery {

    private static final Logger log = LoggerFactory.getLogger(TruthOrDareQuery.class);

    public InlineQueryResult processUpdate(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton more = new InlineKeyboardButton("\uD83C\uDF10 Ще!");
        more.setSwitchInlineQueryCurrentChat("Правда або дія");
        firstRow.add(more);
        keyboard.add(firstRow);

        return InlineQueryResultArticle.builder()
                .id("truth_or_dare")
                .thumbnailUrl("https://play-lh.googleusercontent.com/7HzsLlvflOnqgjI6Rk5nC1Lb_cgBa6E0i9GVH4EfNO1HTWUVH77suiLE89CKoHIJyQ=w240-h480-rw")
                .title("Правда або дія")
                .description("Ви отримаєте або запитання, на яке маєте відповісти; або завдання, яке вам потрібно виконати. Хай щастить!")
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText("@" + update.getInlineQuery().getFrom().getUserName() + "\n\n" + getTruthOrDare())
                        .build())
                .replyMarkup(new InlineKeyboardMarkup(keyboard))
                .build();
    }

    private String getTruthOrDare() {
        String filePath = "truthOrDare.json";
        String jsonContent = "";
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            log.error("Error reading file", e);
        }
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONArray questions = jsonObject.getJSONArray("questions");
        JSONArray dares = jsonObject.getJSONArray("dares");
        Random random = new Random();
        if (random.nextBoolean()) {
            int randomIndex = random.nextInt(questions.length());
            return "Правда: " + questions.getString(randomIndex);
        } else {
            int randomIndex = random.nextInt(dares.length());
            return "Дія: " + dares.getString(randomIndex);
        }
    }

}
