package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.telegram.ai.Gemini;
import dev.ua.ikeepcalm.lumios.telegram.ai.OpenAI;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@BotUpdate
@Component
public class AssistantUpdate extends ServicesShortcut implements Interaction {

    @Value("${telegram.bot.username}")
    private String botName;
    private final OpenAI openAI;
    private final Gemini gemini;

    public AssistantUpdate(OpenAI openAI, Gemini gemini) {
        this.openAI = openAI;
        this.gemini = gemini;
    }

    @Override
    public void fireInteraction(Update update) {
        String message = update.getMessage().getText();

        LumiosChat chat;
        try {
            chat = chatService.findByChatId(update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Failed to get chat by chatId", e);
            return;
        }

        if (message != null && chat.isAiEnabled() && message.matches(".*\\B" + botName + "\\b.*")) {
            String inputText = message.replace(botName, "").trim();
            if (inputText.isBlank() || inputText.length() < 2 || inputText.length() > 1000) {
                sendMessage("Некоректний текст для відправлення на обробку", update.getMessage());
                return;
            }

            if (chat.getCommunicationLimit() <= 0) {
                sendMessage("Ви вже використали всі спроби спілкування з ботом на сьогодні!", update.getMessage());
                return;
            }

            try {
                telegramClient.execute(SendChatAction.builder()
                        .action(String.valueOf(ActionType.TYPING))
                        .chatId(update.getMessage().getChatId())
                        .build());
            } catch (TelegramApiException e) {
                log.error("Failed to send chat action", e);
            }

            if (chat.getAiModel() == null) {
                chat.setAiModel(AiModel.OPENAI);
            }

            String tag = "@" + update.getMessage().getFrom().getUserName();
            String fullName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();

            switch (chat.getAiModel()) {
                case GEMINI -> {
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
                }
                case OPENAI -> {
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
            }
        }
    }

}
