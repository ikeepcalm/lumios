package dev.ua.ikeepcalm.lumios.telegram;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TelegramClient extends OkHttpTelegramClient {

    private final ChatService chatService;
    private final RecordService recordService;

    public TelegramClient(@Value(value = "${telegram.bot.token}") String botToken,
                          ChatService chatService,
                          RecordService recordService) {
        super(botToken);
        this.chatService = chatService;
        this.recordService = recordService;
        initializeBotCommands();
    }

    private void initializeBotCommands() {
        try {
            execute(createBotCommands());
            log.info("Bot commands set successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands", e);
        }
    }

    private SetMyCommands createBotCommands() {
        List<BotCommand> privateCommands = List.of(
                new BotCommand("help", "Відкрити довідку користувача"),
                new BotCommand("tasks", "Відкрити меню завдань"),
                new BotCommand("due", "Переглянути список завдань")
        );

        List<BotCommand> groupCommands = List.of(
                new BotCommand("settings", "Відкрити налаштування бота"),
                new BotCommand("queue", "Створити просту нумеровану чергу"),
                new BotCommand("mixed", "Створити мішану (випадкову) чергу"),
                new BotCommand("editor", "Налаштувати розклад для чату"),
                new BotCommand("today", "Показати розклад на сьогодні"),
                new BotCommand("tomorrow", "Показати розклад на завтра"),
                new BotCommand("now", "Посмлання на заняття, яке йде зараз"),
                new BotCommand("next", "Посмлання на наступне заняття"),
                new BotCommand("week", "Показати розклад тижня"),
                new BotCommand("me", "Подивитися персональну статистику"),
                new BotCommand("stats", "Подивитися загальну статистику"),
                new BotCommand("gamble", "Команда для лудоманів"),
                new BotCommand("gamble_all", "Команда для повних лудоманів"),
                new BotCommand("wheel", "Щоденне колесо фортуни"),
                new BotCommand("identity", "Прив'язати своє реальне ім'я до аккаунту"),
                new BotCommand("import", "Імпортувати розклад для групи з КПІ Кампусу"),
                new BotCommand("summary", "Підведення підсумку за останні повідомлення"),
                new BotCommand("repin", "Прикріпити всі активні черги в групі"),
                new BotCommand("revive", "Відновити всі активні черги в групі"),
                new BotCommand("clearai", "Очистити історію розмови з AI")
        );

        return SetMyCommands.builder()
                .commands(new ArrayList<>(privateCommands))
                .scope(BotCommandScopeAllPrivateChats.builder().build())
                .commands(new ArrayList<>(groupCommands))
                .scope(BotCommandScopeAllGroupChats.builder().build())
                .build();
    }

    // Admin-related methods
    public List<ChatMember> getChatAdministrators(String chatId) {
        try {
            return execute(new GetChatAdministrators(chatId));
        } catch (TelegramApiException e) {
            log.error("Failed to get chat administrators for chat {}", chatId, e);
            return List.of();
        }
    }

    public ChatMember getChatMember(long chatId, long userId) {
        try {
            return execute(new GetChatMember(String.valueOf(chatId), userId));
        } catch (TelegramApiException e) {
            log.error("Failed to get chat member {} in chat {}", userId, chatId, e);
            return null;
        }
    }

    public ChatFullInfo getChat(String chatId) {
        try {
            return execute(new GetChat(chatId));
        } catch (TelegramApiException e) {
            log.error("Failed to get chat {}", chatId, e);
            return null;
        }
    }

    // Message sending methods
    public Message sendTextMessage(TextMessage textMessage) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .text(textMessage.getText())
                .chatId(textMessage.getChatId())
                .replyMarkup(textMessage.getReplyKeyboard());

        if (textMessage.getMessageId() != null) {
            builder.replyToMessageId(textMessage.getMessageId());
        }

        if (textMessage.getParseMode() != null) {
            builder.parseMode(textMessage.getParseMode());
        }

        SendMessage sendMessage = builder.build();

        try {
            Message sentMessage = execute(sendMessage);
            saveMessageRecord(sentMessage, textMessage.getText());
            return sentMessage;
        } catch (TelegramApiException e) {
            if (e instanceof TelegramApiRequestException exception) {
                // Handle Markdown formatting errors
                if (exception.getErrorCode() == 400 && ParseMode.MARKDOWN.equals(textMessage.getParseMode())) {
                    log.warn("Markdown formatting issue, attempting to fix: {}", textMessage.getText());
                    return retryWithFixedMarkdown(textMessage);
                } else if (exception.getErrorCode() == 403) {
                    log.error("Bot was blocked or kicked from chat: {}", textMessage.getChatId());
                } else if (exception.getErrorCode() == 400) {
                    handleInvalidChat(textMessage.getChatId());
                }
            }
            log.error("Failed to send message to chat {}: {}", textMessage.getChatId(), e.getMessage());
            return null;
        }
    }

    private Message retryWithFixedMarkdown(TextMessage textMessage) {
        try {
            // First try with escaped markdown
            TextMessage fixedMessage = TextMessage.builder()
                    .text(escapeMarkdown(textMessage.getText()))
                    .chatId(textMessage.getChatId())
                    .parseMode(textMessage.getParseMode())
                    .replyKeyboard(textMessage.getReplyKeyboard())
                    .messageId(textMessage.getMessageId())
                    .build();

            return execute(SendMessage.builder()
                    .text(fixedMessage.getText())
                    .chatId(fixedMessage.getChatId())
                    .parseMode(fixedMessage.getParseMode())
                    .replyMarkup(fixedMessage.getReplyKeyboard())
                    .replyToMessageId(fixedMessage.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Failed to send with escaped markdown, falling back to plain text");

            // If that fails, try with no parse mode
            try {
                TextMessage plainMessage = TextMessage.builder()
                        .text(textMessage.getText())
                        .chatId(textMessage.getChatId())
                        .parseMode(null)
                        .replyKeyboard(textMessage.getReplyKeyboard())
                        .messageId(textMessage.getMessageId())
                        .build();

                Message sentMessage = execute(SendMessage.builder()
                        .text(plainMessage.getText())
                        .chatId(plainMessage.getChatId())
                        .replyMarkup(plainMessage.getReplyKeyboard())
                        .replyToMessageId(plainMessage.getMessageId())
                        .build());

                saveMessageRecord(sentMessage, plainMessage.getText());
                return sentMessage;
            } catch (TelegramApiException ex) {
                log.error("Failed to send even with plain text", ex);
                return null;
            }
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return null;

        // Escape all Markdown special characters
        Pattern pattern = Pattern.compile("([_*\\[\\]()~`>#+\\-=|{}.!])");
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("\\\\$1");
    }

    private void saveMessageRecord(Message sentMessage, String text) {
        if (sentMessage == null) return;

        try {
            MessageRecord messageRecord = new MessageRecord();
            messageRecord.setChatId(sentMessage.getChatId());
            messageRecord.setMessageId(Long.valueOf(sentMessage.getMessageId()));
            messageRecord.setText(text);
            messageRecord.setDate(LocalDateTime.now());

            if (sentMessage.isReply()) {
                messageRecord.setReplyToMessageId(Long.valueOf(sentMessage.getReplyToMessage().getMessageId()));
            }

            recordService.save(messageRecord);
        } catch (Exception e) {
            log.error("Failed to save message record", e);
        }
    }

    private void handleInvalidChat(long chatId) {
        try {
            LumiosChat chat = chatService.findByChatId(chatId);
            chatService.delete(chat);
            log.info("Deleted invalid chat from database: {}", chatId);
        } catch (NoSuchEntityException ex) {
            log.error("No chat found in database: {}", chatId);
        } catch (Exception e) {
            log.error("Error handling invalid chat", e);
        }
    }

    public Message sendAnimation(MediaMessage mediaMessage) {
        try {
            return execute(SendAnimation.builder()
                    .animation(mediaMessage.getMedia())
                    .chatId(mediaMessage.getChatId())
                    .caption(mediaMessage.getLabel())
                    .replyToMessageId(mediaMessage.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send animation", e);
            return null;
        }
    }

    public Message sendDocument(String chatId, String documentId, String caption) {
        try {
            return execute(SendAnimation.builder()
                    .animation(new InputFile(documentId))
                    .chatId(chatId)
                    .parseMode(ParseMode.MARKDOWN)
                    .showCaptionAboveMedia(true)
                    .caption(caption)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send document", e);
            return null;
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
            log.error("Failed to send photo", e);
            return null;
        }
    }

    public File getFile(String fileId) {
        try {
            return execute(new GetFile(fileId));
        } catch (TelegramApiException e) {
            log.error("Failed to get file", e);
            return null;
        }
    }

    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        try {
            execute(AnswerCallbackQuery.builder()
                    .text(text)
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query", e);
        }
    }

    public Message sendEditMessage(EditMessage editMessage) {
        return sendEditMessage(editMessage, false);
    }

    public Message sendEditMessage(EditMessage editMessage, boolean isCaption) {
        try {
            if (isCaption) {
                return (Message) execute(EditMessageCaption.builder()
                        .messageId(editMessage.getMessageId())
                        .caption(editMessage.getText())
                        .parseMode(editMessage.getParseMode())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            } else if (editMessage.getFilePath() == null && editMessage.getText() == null) {
                return (Message) execute(EditMessageReplyMarkup.builder()
                        .messageId(editMessage.getMessageId())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            } else if (editMessage.getFilePath() == null) {
                return (Message) execute(EditMessageText.builder()
                        .text(editMessage.getText())
                        .messageId(editMessage.getMessageId())
                        .chatId(editMessage.getChatId())
                        .parseMode(editMessage.getParseMode())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .build());
            } else {
                return (Message) execute(EditMessageCaption.builder()
                        .messageId(editMessage.getMessageId())
                        .caption(editMessage.getText())
                        .parseMode(editMessage.getParseMode())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            }
        } catch (TelegramApiException e) {
            log.error("Failed to edit message", e);
            return null;
        } catch (ClassCastException e) {
            log.error("Type conversion error in edit message", e);
            return null;
        }
    }

    public boolean pinChatMessage(long chatId, long messageId) {
        try {
            execute(PinChatMessage.builder()
                    .chatId(chatId)
                    .messageId((int) messageId)
                    .disableNotification(false)
                    .build());
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to pin message {} in chat {}", messageId, chatId, e);
            return false;
        }
    }

    public void sendRemoveMessage(RemoveMessage removeMessage) {
        try {
            execute(DeleteMessage.builder()
                    .chatId(removeMessage.getChatId())
                    .messageId(removeMessage.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to remove message", e);
        }
    }

    public boolean removeMessage(RemoveMessage removeMessage) {
        try {
            execute(DeleteMessage.builder()
                    .chatId(removeMessage.getChatId())
                    .messageId(removeMessage.getMessageId())
                    .build());
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to remove message", e);
            return false;
        }
    }

    public boolean sendReaction(ReactionMessage reactionMessage) {
        try {
            execute(SetMessageReaction.builder()
                    .chatId(reactionMessage.getChatId())
                    .messageId(reactionMessage.getMessageId())
                    .reactionTypes(reactionMessage.getReactionTypes())
                    .build());
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send reaction", e);
            return false;
        }
    }

    public boolean forwardMessage(long chatId, long fromChatId, int messageId) {
        try {
            ForwardMessage forwardMessage = new ForwardMessage(
                    String.valueOf(chatId),
                    String.valueOf(fromChatId),
                    messageId
            );
            forwardMessage.setProtectContent(true);
            execute(forwardMessage);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to forward message", e);
            return false;
        }
    }

    public void sendForwardMessage(long chatId, long fromChatId, int messageId) throws TelegramApiException {
        ForwardMessage forwardMessage = new ForwardMessage(
                String.valueOf(chatId),
                String.valueOf(fromChatId),
                messageId
        );
        forwardMessage.setProtectContent(true);
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to forward message", e);
            throw e;
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