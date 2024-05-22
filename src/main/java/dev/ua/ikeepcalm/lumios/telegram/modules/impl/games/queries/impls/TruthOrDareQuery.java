package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.queries.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.BindService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceBind;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoBindSpecifiedException;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.queries.InlineQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
public class TruthOrDareQuery implements InlineQuery {

    private final BindService bindService;
    private final ChatService chatService;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(TruthOrDareQuery.class);

    public TruthOrDareQuery(BindService bindService, ChatService chatService, UserService userService) {
        this.bindService = bindService;
        this.chatService = chatService;
        this.userService = userService;
    }

    @Transactional
    public InlineQueryResult processUpdate(Update update) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        String result;
        BindResult bindResult;
        try {
            bindResult = retrieveBind(update.getInlineQuery().getFrom().getId());
            result = "@" + update.getInlineQuery().getFrom().getUserName() + "- вам випало: \n\n" + getTruthOrDare() +
                     "\n\n" + "Наступний гравець: @" + getRandomElement(bindResult.chat.getUsers()).getUsername() + "!";
        } catch (NoBindSpecifiedException e) {
            result = "@" + update.getInlineQuery().getFrom().getUserName() + "- вам випало: \n\n" + getTruthOrDare();
        }

        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton more = new InlineKeyboardButton("\uD83C\uDF10 Ще!");
        more.setSwitchInlineQueryCurrentChat("Правда або дія");
        firstRow.add(more);
        keyboard.add(firstRow);

        return InlineQueryResultArticle.builder()
                .id("truth_or_dare")
                .thumbnailUrl("https://naurok.com.ua/uploads/files/888339/377247/432424_html/images/377247%201.png")
                .title("Правда або дія")
                .description("Ви отримаєте або запитання, на яке маєте відповісти; або завдання, яке вам потрібно виконати. Хай щастить!")
                .inputMessageContent(InputTextMessageContent.builder()
                        .messageText(result)
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
            return "\uD83C\uDF40 - Правда: " + questions.getString(randomIndex);
        } else {
            int randomIndex = random.nextInt(dares.length());
            return "\uD83D\uDC40 - Дія: " + dares.getString(randomIndex);
        }
    }


    private BindResult retrieveBind(long userId) throws NoBindSpecifiedException {
        ReverenceBind bind;

        try {
            bind = bindService.findByUserId(userId);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No bind specified for user " + userId);
        }

        ReverenceChat chat;
        try {
            chat = chatService.findByChatId(bind.getChatId());
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No chat specified for user bind " + bind.getUserId());
        }

        ReverenceUser user;
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

    private record BindResult(ReverenceUser user, ReverenceChat chat) {
    }

}
