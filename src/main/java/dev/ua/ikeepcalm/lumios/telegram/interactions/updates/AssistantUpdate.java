package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.ai.Gemini;
import dev.ua.ikeepcalm.lumios.telegram.ai.OpenAI;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.BotDetectionUtils;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@BotUpdate
@Component
public class AssistantUpdate extends ServicesShortcut implements Interaction {

    private final String botName;
    private final OpenAI openAI;
    private final Gemini gemini;
    private final Environment environment;

    private static final int MAX_CONCURRENT_REQUESTS = 5;
    private final AtomicInteger activeRequests = new AtomicInteger(0);

    private static final int MAX_IMAGE_SIZE_BYTES = 1024 * 1024; // 1MB

    public AssistantUpdate(OpenAI openAI, Gemini gemini, Environment environment) {
        this.openAI = openAI;
        this.gemini = gemini;
        this.environment = environment;
        this.botName = environment.getProperty("TELEGRAM_USERNAME");
    }

    @Override
    public void fireInteraction(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        LumiosChat chat = getOrCreateChat(update.getMessage().getChatId(), update.getMessage().getChat().getTitle());

        if (!chat.isAiEnabled()) {
            return;
        }

        boolean hasText = update.getMessage().hasText();
        boolean hasPhoto = update.getMessage().hasPhoto();

        if (!hasText && !hasPhoto) {
            return;
        }

        String textMessage = hasText ? update.getMessage().getText() : update.getMessage().getCaption();

        if (textMessage == null) {
            return;
        }

        boolean isBotMentioned = BotDetectionUtils.isBotMentionedInText(textMessage, botName, chat);
        boolean isReplyToBot = BotDetectionUtils.isReplyToBot(update.getMessage(), botName);
        
        boolean isReplyToMessage = update.getMessage().isReply() && 
                !update.getMessage().getReplyToMessage().getFrom().getIsBot();

        boolean hasRepliedImageToAnalyze = isReplyToMessage && 
                update.getMessage().getReplyToMessage().hasPhoto() && 
                isBotMentioned;

        if (isBotMentioned || isReplyToBot) {
            if (activeRequests.get() >= MAX_CONCURRENT_REQUESTS) {
                sendMessage("Я зараз опрацьовую багато запитів. Будь ласка, спробуйте пізніше.", update.getMessage());
                return;
            }

            String inputText;

            if (isBotMentioned) {
                inputText = textMessage.replace(botName, "").trim();
                
                if (chat.getBotNickname() != null && !chat.getBotNickname().trim().isEmpty()) {
                    inputText = inputText.replaceAll("(?i)" + chat.getBotNickname().trim(), "").trim();
                }

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

            saveUserMessage(update.getMessage(), inputText);

            // Get user context for enhanced AI responses
            LumiosUser currentUser = null;
            try {
                currentUser = userService.findById(update.getMessage().getFrom().getId(), chat);
            } catch (Exception e) {
                log.warn("Could not find user for AI context: {}", e.getMessage());
            }

            String tag = "@" + update.getMessage().getFrom().getUserName();
            String fullName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
            fullName = fullName.replace("null", "");

            byte[] imageData = null;
            boolean shouldProcessImage = (hasPhoto || hasRepliedImageToAnalyze) && chat.getAiModel() == AiModel.GEMINI;
            
            if (shouldProcessImage) {
                try {
                    if (hasPhoto) {
                        imageData = downloadPhoto(update);
                    } else if (hasRepliedImageToAnalyze) {
                        imageData = downloadPhotoFromReply(update);
                    }
                    
                    if (imageData != null && imageData.length > MAX_IMAGE_SIZE_BYTES) {
                        log.warn("Large image detected: {} bytes - consider implementing resizing", imageData.length);
                    }
                } catch (Exception e) {
                    log.error("Failed to download photo", e);
                    sendMessage("Не вдалося завантажити зображення", update.getMessage());
                    return;
                }
            }

            String formattedInput = inputText + ", каже " + fullName + "(" + tag + ")";

            activeRequests.incrementAndGet();

            try {
                switch (chat.getAiModel()) {
                    case GEMINI -> {
                        CompletableFuture<String> responseFuture;

                        if (isReplyToBot) {
                            Long replyToMessageId = Long.valueOf(update.getMessage().getReplyToMessage().getMessageId());
                            log.info("Processing reply to bot, message ID: {}", replyToMessageId);
                            if (shouldProcessImage) {
                                responseFuture = gemini.getChatResponse(formattedInput, update.getMessage().getChatId(), imageData, replyToMessageId, currentUser, chat);
                            } else {
                                responseFuture = gemini.getChatResponseForReply(formattedInput, update.getMessage().getChatId(), replyToMessageId, currentUser, chat);
                            }
                        } else {
                            responseFuture = gemini.getChatResponse(formattedInput, update.getMessage().getChatId(), imageData, currentUser, chat);
                        }

                        responseFuture.thenAccept(response -> {
                            try {
                                if (response != null) {
                                    Message sentMessage = sendMessage(MessageFormatter.sanitizeMarkdownV2(response), ParseMode.MARKDOWNV2, update.getMessage());

                                    if (sentMessage != null) {
                                        try {
                                            MessageRecord botMessageRecord = new MessageRecord();
                                            botMessageRecord.setMessageId(Long.valueOf(sentMessage.getMessageId()));
                                            botMessageRecord.setChatId(chat.getChatId());
                                            botMessageRecord.setText(response);
                                            botMessageRecord.setDate(LocalDateTime.now());
                                            if (isReplyToBot) {
                                                botMessageRecord.setReplyToMessageId(Long.valueOf(update.getMessage().getReplyToMessage().getMessageId()));
                                            }
                                            recordService.save(botMessageRecord);
                                        } catch (Exception e) {
                                            log.error("Failed to save bot message to database", e);
                                        }
                                    }

                                    chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                    chatService.save(chat);
                                }
                            } finally {
                                activeRequests.decrementAndGet();
                            }
                        }).exceptionally(ex -> {
                            activeRequests.decrementAndGet();
                            log.error("Failed to get response from Gemini", ex);
                            sendMessage("Виникла помилка при спробі взаємодії з Gemini. Скоріше за все, перевищення ліміту на хвилину / годину / день. Спробуйте пізніше!", update.getMessage());
                            return null;
                        });
                    }
                    case OPENAI -> {
                        if (shouldProcessImage) {
                            sendMessage("На жаль, OpenAI модель не підтримує обробку зображень. Змініть модель на Gemini для цього функціоналу.", update.getMessage());
                            activeRequests.decrementAndGet();
                            return;
                        }

                        openAI.getChatResponse(formattedInput, chat.getChatId())
                                .thenAccept(response -> {
                                    try {
                                        if (response != null) {
                                            Message sentMessage = sendMessage(MessageFormatter.sanitizeMarkdownV2(response), ParseMode.MARKDOWNV2, update.getMessage());
                                            
                                            if (sentMessage != null) {
                                                try {
                                                    MessageRecord botMessageRecord = new MessageRecord();
                                                    botMessageRecord.setMessageId(Long.valueOf(sentMessage.getMessageId()));
                                                    botMessageRecord.setChatId(chat.getChatId());
                                                    botMessageRecord.setText(response);
                                                    botMessageRecord.setDate(LocalDateTime.now());
                                                    recordService.save(botMessageRecord);
                                                } catch (Exception e) {
                                                    log.error("Failed to save bot message to database", e);
                                                }
                                            }
                                            
                                            chat.setCommunicationLimit(chat.getCommunicationLimit() - 1);
                                            chatService.save(chat);
                                        }
                                    } finally {
                                        activeRequests.decrementAndGet();
                                    }
                                }).exceptionally(ex -> {
                                    activeRequests.decrementAndGet();
                                    log.error("Failed to get response from OpenAI", ex);
                                    sendMessage("Виникла помилка при спробі взаємодії з Open AI.", update.getMessage());
                                    return null;
                                });
                    }
                }
            } catch (Exception e) {
                activeRequests.decrementAndGet();
                log.error("Unexpected error during AI processing", e);
                sendMessage("Виникла неочікувана помилка при обробці запиту.", update.getMessage());
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
        return downloadPhotoFromPhotoList(photos);
    }

    private byte[] downloadPhotoFromReply(Update update) throws TelegramApiException, IOException {
        if (!update.getMessage().isReply() || !update.getMessage().getReplyToMessage().hasPhoto()) {
            throw new IllegalArgumentException("Reply message does not contain photo");
        }
        
        List<PhotoSize> photos = update.getMessage().getReplyToMessage().getPhoto();
        return downloadPhotoFromPhotoList(photos);
    }

    private byte[] downloadPhotoFromPhotoList(List<PhotoSize> photos) throws TelegramApiException, IOException {
        PhotoSize selectedPhoto;
        if (photos.size() > 1) {
            selectedPhoto = photos.get(photos.size() - 2);
        } else {
            selectedPhoto = photos.getFirst();
        }

        log.info("Selected photo size: {}x{}, file_id: {}",
                selectedPhoto.getWidth(), selectedPhoto.getHeight(), selectedPhoto.getFileId());

        GetFile getFileRequest = new GetFile(selectedPhoto.getFileId());
        File file = telegramClient.execute(getFileRequest);

        String fileUrl = "https://api.telegram.org/file/bot" + environment.getProperty("TELEGRAM_TOKEN") + "/" + file.getFilePath();
        URL url = new URL(fileUrl);

        try (InputStream is = url.openStream()) {
            byte[] imageData = is.readAllBytes();
            log.info("Downloaded image size: {} bytes", imageData.length);
            return imageData;
        }
    }


    private LumiosChat getOrCreateChat(Long chatId, String chatTitle) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            chat = new LumiosChat();
            chat.setChatId(chatId);
            chat.setName(chatTitle != null ? chatTitle : "Chat " + chatId);
            chat.setTimetableEnabled(true);
            chatService.save(chat);
            log.info("Created new chat with ID: {}", chatId);
        }
        return chat;
    }
}