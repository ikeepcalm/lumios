package dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.queries;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.BindService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosBind;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoBindSpecifiedException;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotInlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.interactions.inlines.InlineQuery;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@BotInlineQuery(inlineQuery = "Правда або дія")
public class TruthOrDareQuery implements InlineQuery {

    private static final Logger log = LoggerFactory.getLogger(TruthOrDareQuery.class);
    private final BindService bindService;
    private final ChatService chatService;
    private final UserService userService;
    private final TranslationService translationService;

    public TruthOrDareQuery(BindService bindService, ChatService chatService, UserService userService, TranslationService translationService) {
        this.bindService = bindService;
        this.chatService = chatService;
        this.userService = userService;
        this.translationService = translationService;
    }


    public InlineQueryResult processUpdate(Update update) {
        String lang = update.getInlineQuery().getFrom().getLanguageCode();
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        String result;
        BindResult bindResult;
        try {
            bindResult = retrieveBind(update.getInlineQuery().getFrom().getId());
            result = translationService.getMessage("inline.truth_or_dare.result", lang, update.getInlineQuery().getFrom().getUserName()) 
                     + getTruthOrDare(lang) 
                     + translationService.getMessage("inline.truth_or_dare.next_player", lang, getRandomElement(bindResult.chat.getUsers()).getUsername());
        } catch (NoBindSpecifiedException e) {
            result = translationService.getMessage("inline.truth_or_dare.result", lang, update.getInlineQuery().getFrom().getUserName()) 
                     + getTruthOrDare(lang);
        }

        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton more = new InlineKeyboardButton(translationService.getMessage("inline.truth_or_dare.more", lang));
        more.setSwitchInlineQueryCurrentChat("Правда або дія");
        firstRow.add(more);
        keyboard.add(firstRow);

        return InlineQueryResultArticle.builder()
                .id("truth_or_dare")
                .thumbnailUrl("https://naurok.com.ua/uploads/files/888339/377247/432424_html/images/377247%201.png")
                .title(translationService.getMessage("inline.truth_or_dare.title", lang))
                .description(translationService.getMessage("inline.truth_or_dare.desc", lang))
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(result)
                        .build())
                .replyMarkup(new InlineKeyboardMarkup(keyboard))
                .build();
    }

    private String getTruthOrDare(String lang) {
        String fileName;
        if ("en".equals(lang)) {
            fileName = "truthOrDare_en.json";
        } else if ("zh".equals(lang)) {
            fileName = "truthOrDare_zh.json";
        } else {
            fileName = "truthOrDare.json";
        }
        try (var is = getClass().getClassLoader().getResourceAsStream("truth-or-dare/" + fileName)) {
            if (is == null) {
                return translationService.getMessage("inline.truth_or_dare.empty_db", lang);
            }
            String jsonContent = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonContent);
            JSONArray questions = jsonObject.getJSONArray("questions");
            JSONArray dares = jsonObject.getJSONArray("dares");
            Random random = new Random();
            if (random.nextBoolean()) {
                int randomIndex = random.nextInt(questions.length());
                return translationService.getMessage("inline.truth_or_dare.truth_prefix", lang) + questions.getString(randomIndex);
            } else {
                int randomIndex = random.nextInt(dares.length());
                return translationService.getMessage("inline.truth_or_dare.dare_prefix", lang) + dares.getString(randomIndex);
            }
        } catch (IOException e) {
            log.error("Failed to load truth or dare file: {}", fileName, e);
            return translationService.getMessage("inline.truth_or_dare.empty_db", lang);
        }
    }


    private BindResult retrieveBind(long userId) throws NoBindSpecifiedException {
        LumiosBind bind;

        try {
            bind = bindService.findByUserId(userId);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No bind specified for user " + userId);
        }

        LumiosChat chat;
        try {
            chat = chatService.findByChatId(bind.getChatId());
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No chat specified for user bind " + bind.getUserId());
        }

        LumiosUser user;
        try {
            user = userService.findById(userId, chat);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No user specified for user bind " + bind.getUserId());
        }

        return new BindResult(user, chat);
    }

    private <T> T getRandomElement(Set<T> set) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("The Set cannot be empty.");
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(set.size());
        int i = 0;
        for (T element : set) {
            if (i == randomIndex) {
                return element;
            }
            i++;
        }
        throw new IllegalStateException("Something went wrong while picking a random element.");
    }

    private record BindResult(LumiosUser user, LumiosChat chat) {
    }

}
