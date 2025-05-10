package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
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
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        // Skip processing if not a message
        if (!update.hasMessage()) {
            return;
        }

        LumiosChat chat;
        try {
            chat = chatService.findByChatId(update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Failed to get chat by chatId", e);
            return;
        }

        if (!chat.isAiEnabled()) {
            return;
        }

        boolean hasText = update.getMessage().hasText();
        boolean hasPhoto = update.getMessage().hasPhoto();

        if (!hasText && !hasPhoto) {
            return;
        }

        String textMessage = hasText ? update.getMessage().getText() : "";

        boolean isBotMentioned = hasText && textMessage.matches(".*\\B" + botName + "\\b.*");
        boolean isReplyToBot = update.getMessage().isReply() &&
                update.getMessage().getReplyToMessage().getFrom().getIsBot() &&
                update.getMessage().getReplyToMessage().getFrom().getUserName().equals(botName.replace("@", ""));

        if (isBotMentioned || isReplyToBot) {
            String inputText;

            if (isBotMentioned) {
                inputText = textMessage.replace(botName, "").trim();

                if (inputText.isBlank() && update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().hasText()) {
                    inputText = update.getMessage().getReplyToMessage().getText();
                }
            } else {
                inputText = hasText ? textMessage : "Опиши, що зображено на фото";
            }

            if (!hasPhoto && (inputText.isBlank() || inputText.length() < 2 || inputText.length() > 1000)) {
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

            // Save user message to database for context
            saveUserMessage(update.getMessage(), inputText);

            String tag = "@" + update.getMessage().getFrom().getUserName();
            String fullName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            fullName = fullName.replace("null", "");

            // Process image if present
            byte[] imageData = null;
            if (hasPhoto && chat.getAiModel() == AiModel.GEMINI) {
                try {
                    imageData = downloadPhoto(update);
                } catch (Exception e) {
                    log.error("Failed to download photo", e);
                    sendMessage("Не вдалося завантажити зображення", update.getMessage());
                    return;
                }
            }

            String formattedInput = inputText + ", каже " + fullName + "(" + tag + ")";

            switch (chat.getAiModel()) {
                case GEMINI -> {
                    CompletableFuture<String> responseFuture;

                    if (isReplyToBot) {
                        Long replyToMessageId = Long.valueOf(update.getMessage().getReplyToMessage().getMessageId());
                        log.info("Processing reply to bot, message ID: {}", replyToMessageId);
                        if (hasPhoto) {
                            responseFuture = gemini.getChatResponse(formattedInput, update.getMessage().getChatId(), imageData, replyToMessageId);
                        } else {
                            responseFuture = gemini.getChatResponseForReply(formattedInput, update.getMessage().getChatId(), replyToMessageId);
                        }
                    } else {
                        responseFuture = gemini.getChatResponse(formattedInput, update.getMessage().getChatId(), imageData);
                    }

                    responseFuture.thenAccept(response -> {
                        if (response != null) {
                            Message sentMessage = sendMessage(response, ParseMode.MARKDOWN, update.getMessage());

                            // Update the message ID in the database
                            if (sentMessage != null) {
                                try {
                                    List<MessageRecord> records = recordService.findLastMessagesByChatId(chat.getChatId(), 1);
                                    if (!records.isEmpty()) {
                                        MessageRecord record = records.getFirst();
                                        if (record.getUser() == null && record.getMessageId() == null) {
                                            record.setMessageId(Long.valueOf(sentMessage.getMessageId()));
                                            recordService.save(record);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to update message ID in database", e);
                                }
                            }

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
                    if (hasPhoto) {
                        sendMessage("На жаль, OpenAI модель не підтримує обробку зображень. Змініть модель на Gemini для цього функціоналу.", update.getMessage());
                        return;
                    }

                    openAI.getChatResponse(formattedInput, chat.getChatId())
                            .thenAccept(response -> {
                                if (response != null) {
                                    Message sentMessage = sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
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

    private void saveUserMessage(Message message, String inputText) {
        try {
            LumiosChat lumiosChat = chatService.findByChatId(message.getChatId());
            LumiosUser user = userService.findById(message.getFrom().getId(), lumiosChat);

            MessageRecord messageRecord = new MessageRecord();
            messageRecord.setMessageId(Long.valueOf(message.getMessageId()));
            messageRecord.setChatId(message.getChatId());
            messageRecord.setText(inputText);
            messageRecord.setDate(LocalDateTime.now());
            messageRecord.setUser(user);

            if (message.getReplyToMessage() != null) {
                messageRecord.setReplyToMessageId(Long.valueOf(message.getReplyToMessage().getMessageId()));
            }

            recordService.save(messageRecord);
        } catch (Exception e) {
            log.error("Failed to save user message to database", e);
        }
    }

    private byte[] downloadPhoto(Update update) throws TelegramApiException, IOException {
        List<PhotoSize> photos = update.getMessage().getPhoto();
        PhotoSize largestPhoto = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new IllegalStateException("No photo found"));

        log.info("Selected photo size: {}x{}, file_id: {}",
                largestPhoto.getWidth(), largestPhoto.getHeight(), largestPhoto.getFileId());

        GetFile getFileRequest = new GetFile(largestPhoto.getFileId());
        File file = telegramClient.execute(getFileRequest);

        String fileUrl = "https://api.telegram.org/file/bot" + environment.getProperty("TELEGRAM_TOKEN") + "/" + file.getFilePath();
        URL url = new URL(fileUrl);

        try (InputStream is = url.openStream()) {
            byte[] imageData = is.readAllBytes();
            log.info("Downloaded image size: {} bytes", imageData.length);
            return imageData;
        }
    }
}