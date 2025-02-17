package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.telegram.ai.Gemini;
import dev.ua.ikeepcalm.lumios.telegram.ai.OpenAI;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;

@Slf4j
@BotUpdate
@Component
public class AssistantUpdate extends ServicesShortcut implements Interaction {

    private String botName;
    private final OpenAI openAI;
    private final Gemini gemini;
    private final Environment environment;

    public AssistantUpdate(OpenAI openAI, Gemini gemini, Environment environment) {
        this.openAI = openAI;
        this.gemini = gemini;
        this.environment = environment;
        this.botName = environment.getProperty("TELEGRAM_USERNAME");
    }

    @Override
    public void fireInteraction(Update update) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Failed to get chat by chatId", e);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        if (!chat.isAiEnabled()) {
            return;
        }

        String textMessage = update.getMessage().getText();

        if (textMessage.matches(".*\\B" + botName + "\\b.*")) {
            String inputText = textMessage.replace(botName, "").trim();

            if (inputText.isBlank() && update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().hasText()) {
                inputText = update.getMessage().getReplyToMessage().getText();
            }

            if (inputText.isBlank() || inputText.length() < 2 || inputText.length() > 1000) {
                sendMessage("Некоректний текст для відправлення на обробку", update.getMessage());
                return;
            }

            if (chat.getCommunicationLimit() <= 0) {
                sendMessage("Ви вже використали всі спроби спілкування з ботом на сьогодні!", update.getMessage());
                return;
            }

            try {
                telegramClient.execute(SendChatAction.builder().action(String.valueOf(ActionType.TYPING)).chatId(update.getMessage().getChatId()).build());
            } catch (TelegramApiException e) {
                log.error("Failed to send chat action", e);
            }

            if (chat.getAiModel() == null) {
                chat.setAiModel(AiModel.OPENAI);
            }

            String tag = "@" + update.getMessage().getFrom().getUserName();
            String fullName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            fullName = fullName.replace("null", "");

            switch (chat.getAiModel()) {
                case GEMINI ->
                        gemini.getChatResponse(inputText + ", каже " + fullName + "(" + tag + ")").thenAccept(response -> {
                            if (response != null) {
                                sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                                chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                chatService.save(chat);
                            }
                        }).exceptionally(ex -> {
                            log.error("Failed to get response from Gemini", ex);
                            sendMessage("Виникла помилка при спробі взаємодії з Gemini. Скоріше за все, перевищення ліміту на хвилину / годину / день. Спробуйте пізніше!", update.getMessage());
                            return null;
                        });
                case OPENAI ->
                        openAI.getChatResponse(inputText + ", каже " + fullName + "(@" + tag, chat.getChatId()).thenAccept(response -> {
                            if (response != null) {
                                sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                                chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                chatService.save(chat);
                            }
                        }).exceptionally(ex -> {
                            log.error("Failed to get response from OpenAI", ex);
                            sendMessage("Виникла помилка при спробі взаємодії з Open AI.", update.getMessage());
                            return null;
                        });
            }
        } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getFrom().getIsBot()) {
            if (chat.getCommunicationLimit() <= 0) {
                return;
            }

            if (!update.getMessage().getReplyToMessage().getFrom().getUserName().equals(botName.replace("@", ""))) {
                return;
            }

            String tag = "@" + update.getMessage().getFrom().getUserName();

            List<MessageRecord> context = recordService.findAllInReplyChain(chat.getChatId(), Long.valueOf(update.getMessage().getReplyToMessage().getMessageId()));

            Collections.reverse(context);

            StringBuilder stringBuilder = new StringBuilder();
            for (MessageRecord messageRecord : context) {
                stringBuilder.append("@").append(messageRecord.getUser().getUsername()).append(" каже: ").append(messageRecord.getText()).append("\n");
            }

            String invertedContext = stringBuilder.toString();

            try {
                telegramClient.execute(SendChatAction.builder().action(String.valueOf(ActionType.TYPING)).chatId(update.getMessage().getChatId()).build());
            } catch (TelegramApiException e) {
                log.error("Failed to send chat action", e);
            }

            if (chat.getAiModel() == null) {
                chat.setAiModel(AiModel.OPENAI);
            }

            switch (chat.getAiModel()) {
                case GEMINI ->
                        gemini.getChatResponse("Контекст розмови: " + invertedContext + "\n" + "На що " + tag + " відповідає " + textMessage).thenAccept(response -> {
                            if (response != null) {
                                sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                                chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                chatService.save(chat);
                            }
                        }).exceptionally(ex -> {
                            log.error("Failed to get response from Gemini", ex);
                            sendMessage("Виникла помилка при спробі взаємодії з Gemini. Скоріше за все, перевищення ліміту на хвилину / годину / день. Спробуйте пізніше!", update.getMessage());
                            return null;
                        });
                case OPENAI ->
                        openAI.getChatResponse("Контекст розмови: " + invertedContext + "\n" + "На що " + tag + " відповідає " + textMessage, chat.getChatId()).thenAccept(response -> {
                            if (response != null) {
                                sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                                chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                chatService.save(chat);
                            }
                        }).exceptionally(ex -> {
                            log.error("Failed to get response from OpenAI", ex);
                            sendMessage("Виникла помилка при спробі взаємодії з Open AI.", update.getMessage());
                            return null;
                        });
            }
        }
    }

}
