package dev.ua.ikeepcalm.lumios.telegram;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.MessageProcessingException;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.TelegramApiFailedException;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramClient extends OkHttpTelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);

    private final ChatService chatService;
    private final RecordService recordService;

    public TelegramClient(@Value(value = "${telegram.bot.token}") String botToken, ChatService chatService, RecordService recordService) {
        super(botToken);
        try {
            executeCommand(setBotCommands());
            log.info("Bot commands set successfully");
        } catch (TelegramApiFailedException e) {
            log.error("Failed to set bot commands", e);
        }
        this.chatService = chatService;
        this.recordService = recordService;
    }

    private SetMyCommands setBotCommands() {
        return SetMyCommands.builder()
                .commands(
                        new ArrayList<>(List.of(
                                new BotCommand("help", "Відкрити довідку користувача"),
                                new BotCommand("tasks", "Відкрити меню завдань"),
                                new BotCommand("due", "Переглянути список завдань"),
                                new BotCommand("me", "Подивитися персональну статистику"),
                                new BotCommand("stats", "Подивитися загальну статистику"),
                                new BotCommand("gamble", "Команда для лудоманів"),
                                new BotCommand("wheel", "Щоденне колесо фортуни")
                        ))).scope(BotCommandScopeAllPrivateChats.builder().build())
                .commands(
                        new ArrayList<>(List.of(
                                // System Commands
                                new BotCommand("settings", "Відкрити налаштування бота"),
                                new BotCommand("help", "Відкрити довідку користувача"),
                                new BotCommand("summary", "Підведення підсумку за останні повідомлення"),
                                
                                // Queue Commands
                                new BotCommand("queue", "Створити просту нумеровану чергу"),
                                new BotCommand("mixed", "Створити мішану (випадкову) чергу"),
                                new BotCommand("identity", "Прив'язати своє реальне ім'я до аккаунту"),
                                new BotCommand("repin", "Прикріпити всі активні черги в групі (для адмінів)"),
                                new BotCommand("revive", "Відновити всі активні черги в групі (для адмінів)"),
                                
                                // Timetable Commands  
                                new BotCommand("editor", "Налаштувати розклад для чату"),
                                new BotCommand("today", "Показати розклад на сьогодні"),
                                new BotCommand("tomorrow", "Показати розклад на завтра"),
                                new BotCommand("week", "Показати розклад тижня"),
                                new BotCommand("now", "Посилання на заняття, яке йде зараз"),
                                new BotCommand("next", "Посилання на наступне заняття"),
                                new BotCommand("import", "Імпортувати розклад для групи з КПІ Кампусу"),
                                
                                // Task Commands
                                new BotCommand("tasks", "Відкрити меню завдань"),
                                new BotCommand("due", "Переглянути список завдань"),
                                
                                // Reverence Commands
                                new BotCommand("me", "Подивитися персональну статистику"),
                                new BotCommand("stats", "Подивитися загальну статистику"),
                                new BotCommand("gamble", "Команда для лудоманів"),
                                new BotCommand("gamble_all", "Команда для повних лудоманів"),
                                new BotCommand("wheel", "Щоденне колесо фортуни"),

                                // Ai
                                new BotCommand("nickname", "Встановити нікнейм для бота")
                        ))
                ).scope(BotCommandScopeAllGroupChats.builder().build()).build();
    }

    private Object executeCommand(BotApiMethod<?> command) throws TelegramApiFailedException {
        try {
            return execute(command);
        } catch (TelegramApiRequestException e) {
            log.error("Failed to execute {} - Code: {}, Message: {}", 
                command.getMethod(), e.getErrorCode(), e.getApiResponse());
            throw new TelegramApiFailedException(
                "Failed to execute " + command.getMethod(),
                e.getErrorCode(),
                e.getApiResponse()
            );
        } catch (TelegramApiException e) {
            log.error("Unexpected API error executing {}", command.getMethod(), e);
            throw new TelegramApiFailedException(
                "Unexpected error executing " + command.getMethod(), e
            );
        }
    }

    public List<ChatMember> getChatAdministrators(String chatId) throws TelegramApiException {
        ArrayList<ChatMember> chats;
        chats = execute(new GetChatAdministrators(chatId));
        return chats;
    }

    public ChatMember getChatMember(long chatId, long userId) throws TelegramApiFailedException {
        try {
            return execute(new GetChatMember(String.valueOf(chatId), userId));
        } catch (TelegramApiRequestException e) {
            log.error("Failed to get chat member for chat {} user {}", chatId, userId, e);
            throw new TelegramApiFailedException(
                "Failed to get chat member",
                e.getErrorCode(),
                e.getApiResponse()
            );
        } catch (TelegramApiException e) {
            log.error("Unexpected error getting chat member for chat {} user {}", chatId, userId, e);
            throw new TelegramApiFailedException("Unexpected error getting chat member", e);
        }
    }

    public Message sendAnimation(MediaMessage mediaMessage) throws MessageProcessingException {
        try {
            return execute(SendAnimation.builder()
                    .animation(mediaMessage.getMedia())
                    .chatId(mediaMessage.getChatId())
                    .caption(mediaMessage.getLabel())
                    .replyToMessageId(mediaMessage.getMessageId())
                    .build());
        } catch (TelegramApiRequestException e) {
            log.error("Failed to send animation to chat {}", mediaMessage.getChatId(), e);
            throw new MessageProcessingException(
                "Failed to send animation",
                mediaMessage.getChatId(),
                "animation",
                e
            );
        } catch (TelegramApiException e) {
            log.error("Unexpected error sending animation to chat {}", mediaMessage.getChatId(), e);
            throw new MessageProcessingException(
                "Unexpected error sending animation",
                mediaMessage.getChatId(),
                "animation",
                e
            );
        }
    }

    public Message sendDocument(String chatId, String documentId, String caption) throws MessageProcessingException {
        try {
            return execute(SendAnimation.builder()
                    .animation(new InputFile(documentId))
                    .chatId(chatId)
                    .parseMode(ParseMode.MARKDOWN)
                    .showCaptionAboveMedia(true)
                    .caption(caption)
                    .build());
        } catch (TelegramApiRequestException e) {
            log.error("Failed to send document to chat {}", chatId, e);
            throw new MessageProcessingException(
                "Failed to send document",
                Long.valueOf(chatId),
                "document",
                e
            );
        } catch (TelegramApiException e) {
            log.error("Unexpected error sending document to chat {}", chatId, e);
            throw new MessageProcessingException(
                "Unexpected error sending document",
                Long.valueOf(chatId),
                "document",
                e
            );
        }
    }

    public Message sendPhoto(String chatId, String photoId, String caption) {
        try {
            return execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(photoId))
                    .parseMode(ParseMode.MARKDOWN)
                    .showCaptionAboveMedia(true)
                    .caption(caption)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatFullInfo getChat(String chatId) throws TelegramApiException {
        return execute(new GetChat(chatId));
    }

    public File getFile(String fileId) throws TelegramApiFailedException {
        try {
            return execute(new GetFile(fileId));
        } catch (TelegramApiRequestException e) {
            log.error("Failed to get file {}", fileId, e);
            throw new TelegramApiFailedException(
                "Failed to get file",
                e.getErrorCode(),
                e.getApiResponse()
            );
        } catch (TelegramApiException e) {
            log.error("Unexpected error getting file {}", fileId, e);
            throw new TelegramApiFailedException("Unexpected error getting file", e);
        }
    }

    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        try {
            executeCommand(AnswerCallbackQuery.builder()
                    .text(text)
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiFailedException e) {
            log.warn("Failed to answer callback query {}: {}", callbackQueryId, e.getMessage());
        }
    }

    public Message sendEditMessage(EditMessage editMessage) {
        return sendEditMessage(editMessage, false);
    }

    public Message sendEditMessage(EditMessage editMessage, boolean isCaption) {
        if (isCaption) {
            try {
                return (Message) executeCommand(EditMessageCaption.builder()
                        .messageId(editMessage.getMessageId())
                        .caption(editMessage.getText())
                        .parseMode(editMessage.getParseMode())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            } catch (TelegramApiFailedException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (editMessage.getFilePath() == null && editMessage.getText() == null) {
                try {
                    return (Message) executeCommand(EditMessageReplyMarkup.builder()
                            .messageId(editMessage.getMessageId())
                            .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                            .chatId(editMessage.getChatId())
                            .build());
                } catch (TelegramApiFailedException e) {
                    throw new RuntimeException(e);
                }
            } else if (editMessage.getFilePath() == null) {
                try {
                    return (Message) executeCommand(EditMessageText.builder()
                            .text(editMessage.getText())
                            .messageId(editMessage.getMessageId())
                            .chatId(editMessage.getChatId())
                            .parseMode(editMessage.getParseMode())
                            .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                            .build());
                } catch (TelegramApiFailedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    return (Message) executeCommand(EditMessageCaption.builder()
                            .messageId(editMessage.getMessageId())
                            .caption(editMessage.getText())
                            .parseMode(editMessage.getParseMode())
                            .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                            .chatId(editMessage.getChatId())
                            .build());
                } catch (TelegramApiFailedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void pinChatMessage(long chatId, long messageId) throws TelegramApiException {
        try {
            executeCommand(PinChatMessage.builder()
                    .chatId(chatId)
                    .messageId((int) messageId)
                    .disableNotification(false)
                    .build());
        } catch (TelegramApiFailedException e) {
            throw new TelegramApiException(e);
        }
    }

    public Message sendTextMessage(TextMessage textMessage) {
        return sendTextMessageWithRetry(textMessage, 3);
    }

    private Message sendTextMessageWithRetry(TextMessage textMessage, int maxRetries) {
        int currentRetry = 0;
        String originalParseMode = textMessage.getParseMode();
        
        while (currentRetry < maxRetries) {
            try {
                Message sentMessage = (Message) executeCommand(SendMessage.builder()
                        .text(textMessage.getText())
                        .chatId(textMessage.getChatId())
                        .parseMode(textMessage.getParseMode())
                        .replyMarkup(textMessage.getReplyKeyboard())
                        .replyToMessageId(textMessage.getMessageId())
                        .build());

                recordMessageSent(textMessage, sentMessage);
                return sentMessage;

            } catch (TelegramApiFailedException e) {
                currentRetry++;
                log.warn("Attempt {} failed for chat {}: {}", currentRetry, textMessage.getChatId(), e.getMessage());
                
                if (!handleApiError(e, textMessage, currentRetry)) {
                    break;
                }
            } catch (Exception e) {
                log.error("Unexpected error sending message to chat {}", textMessage.getChatId(), e);
                break;
            }
        }
        
        textMessage.setParseMode(originalParseMode);
        return handleFallbackMessage(textMessage);
    }
    
    private boolean handleApiError(TelegramApiFailedException e, TextMessage textMessage, int retryAttempt) {
        switch (e.getErrorCode()) {
            case 400 -> {
                if (textMessage.getParseMode() != null) {
                    log.info("Parse mode error, retrying without formatting for chat {}", textMessage.getChatId());
                    textMessage.setParseMode(null);
                    return true;
                } else {
                    log.error("Bad request error for chat {}: {}", textMessage.getChatId(), e.getApiResponse());
                    cleanupInvalidChat(textMessage.getChatId());
                    return false;
                }
            }
            case 403 -> {
                log.error("Bot blocked by chat {}: {}", textMessage.getChatId(), e.getApiResponse());
                cleanupInvalidChat(textMessage.getChatId());
                return false;
            }
            case 429 -> {
                int delay = Math.min(1000 * retryAttempt, 5000);
                log.warn("Rate limited, waiting {}ms before retry", delay);
                try {
                    Thread.sleep(delay);
                    return true;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            case 500, 502, 503 -> {
                log.warn("Server error {}, retrying...", e.getErrorCode());
                return true;
            }
            default -> {
                log.error("Unhandled API error {} for chat {}: {}", 
                    e.getErrorCode(), textMessage.getChatId(), e.getApiResponse());
                return false;
            }
        }
    }
    
    private void recordMessageSent(TextMessage textMessage, Message sentMessage) {
        try {
            MessageRecord messageRecord = new MessageRecord();
            messageRecord.setChatId(textMessage.getChatId());
            messageRecord.setMessageId(Long.valueOf(sentMessage.getMessageId()));
            messageRecord.setText(textMessage.getText());
            messageRecord.setDate(LocalDateTime.now());
            if (sentMessage.isReply()) {
                messageRecord.setReplyToMessageId(Long.valueOf(sentMessage.getReplyToMessage().getMessageId()));
            }
            recordService.save(messageRecord);
        } catch (Exception e) {
            log.error("Failed to record message for chat {}", textMessage.getChatId(), e);
        }
    }
    
    private void cleanupInvalidChat(Long chatId) {
        try {
            LumiosChat chat = chatService.findByChatId(chatId);
            chatService.delete(chat);
            log.info("Cleaned up invalid chat: {}", chatId);
        } catch (NoSuchEntityException e) {
            log.debug("Chat {} not found in database during cleanup", chatId);
        } catch (Exception e) {
            log.error("Failed to cleanup chat {}", chatId, e);
        }
    }
    
    private Message handleFallbackMessage(TextMessage textMessage) {
        log.info("All retries failed for chat {}, attempting fallback", textMessage.getChatId());
        
        try {
            textMessage.setText(MessageFormatter.formatErrorMessage(
                "Сталася помилка при відправці повідомлення. Спробуйте ще раз пізніше."
            ));
            textMessage.setParseMode(MessageFormatter.getDefaultParseMode());
            textMessage.setReplyKeyboard(null);
            
            return (Message) executeCommand(SendMessage.builder()
                    .text(textMessage.getText())
                    .chatId(textMessage.getChatId())
                    .parseMode(textMessage.getParseMode())
                    .build());
        } catch (Exception e) {
            log.error("Fallback message also failed for chat {}", textMessage.getChatId(), e);
            return null;
        }
    }

    public void sendRemoveMessage(RemoveMessage removeMessage) throws TelegramApiException {
        try {
            executeCommand(DeleteMessage.builder()
                    .chatId(removeMessage.getChatId())
                    .messageId(removeMessage.getMessageId())
                    .build());
        } catch (TelegramApiFailedException e) {
            throw new TelegramApiException(e);
        }
    }

    public void sendReaction(ReactionMessage reactionMessage) {
        try {
            executeCommand(SetMessageReaction.builder()
                    .chatId(reactionMessage.getChatId())
                    .messageId(reactionMessage.getMessageId())
                    .reactionTypes(reactionMessage.getReactionTypes())
                    .build());
        } catch (TelegramApiFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendForwardMessage(long chatId, long fromChatId, int messageId) throws TelegramApiException {
        ForwardMessage forwardMessage = new ForwardMessage(String.valueOf(chatId), String.valueOf(fromChatId), messageId);
        forwardMessage.setProtectContent(true);
        try {
            executeCommand(forwardMessage);
        } catch (TelegramApiFailedException e) {
            throw new TelegramApiException(e);
        }
    }

    public void sendAnswerInlineQuery(AnswerInlineQuery answerInlineQuery) {
        try {
            execute(answerInlineQuery);
        } catch (TelegramApiException e) {
            log.error("Failed to send answer inline query", e);
        }
    }
}

