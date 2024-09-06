package dev.ua.ikeepcalm.lumios.telegram;

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

import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramClient extends OkHttpTelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);

    public TelegramClient(@Value(value = "${telegram.bot.token}") String botToken) {
        super(botToken);
        try {
            executeCommand(setBotCommands());
            log.info("Bot commands set successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands", e);
        }
    }

    private SetMyCommands setBotCommands() {
        return SetMyCommands.builder()
                .commands(
                        new ArrayList<>(List.of(
                                new BotCommand("help", "Відкрити довідку користувача"),
                                new BotCommand("task", "Відкрити меню завдань"),
                                new BotCommand("due", "Переглянути список завдань")
                        ))).scope(BotCommandScopeAllPrivateChats.builder().build())
                .commands(
                        new ArrayList<>(List.of(
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
                                new BotCommand("import", "Імпортувати розклад для групи з КПІ Кампусу")
                        ))
                ).scope(BotCommandScopeAllGroupChats.builder().build()).build();
    }

    private Object executeCommand(BotApiMethod<?> command) throws TelegramApiException {
        try {
            return execute(command);
        } catch (TelegramApiRequestException e) {
            if (e.getErrorCode() == 400 || e.getErrorCode() == 403) {
                log.error("Failed to execute {}", command.getMethod());
                log.error("Error code: {}", e.getErrorCode());
            }
            throw new TelegramApiException(e);
        }
    }

    public List<ChatMember> getChatAdministrators(String chatId) throws TelegramApiException {
        ArrayList<ChatMember> chats;
        chats = execute(new GetChatAdministrators(chatId));
        return chats;
    }

    public ChatMember getChatMember(long chatId, long userId) {
        try {
            return execute(new GetChatMember(String.valueOf(chatId), userId));
        } catch (TelegramApiException e) {
            log.error("Failed to get chat member", e);
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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

    public File getFile(String fileId) {
        try {
            return execute(new GetFile(fileId));
        } catch (TelegramApiException e) {
            log.error("Failed to get file", e);
            throw new RuntimeException(e);
        }
    }

    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        try {
            executeCommand(AnswerCallbackQuery.builder()
                    .text(text)
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
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
            } catch (TelegramApiException e) {
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
                } catch (TelegramApiException e) {
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
                } catch (TelegramApiException e) {
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
                } catch (TelegramApiException e) {
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
                    .build());
        } catch (TelegramApiException e) {
            throw new TelegramApiException(e);
        }
    }

    public Message sendTextMessage(TextMessage textMessage, boolean shouldHandleExceptions) throws TelegramApiException {
        if (!shouldHandleExceptions) {
            return sendTextMessage(textMessage);
        } else {
            try {
                return (Message) executeCommand(SendMessage.builder()
                        .text(textMessage.getText())
                        .chatId(textMessage.getChatId())
                        .parseMode(textMessage.getParseMode())
                        .replyMarkup(textMessage.getReplyKeyboard())
                        .replyToMessageId(textMessage.getMessageId())
                        .build());
            } catch (TelegramApiException e) {
                throw new TelegramApiException(e);
            }
        }
    }

    public Message sendTextMessage(TextMessage textMessage) {
        try {
            return (Message) executeCommand(SendMessage.builder()
                    .text(textMessage.getText())
                    .chatId(textMessage.getChatId())
                    .parseMode(textMessage.getParseMode())
                    .replyMarkup(textMessage.getReplyKeyboard())
                    .replyToMessageId(textMessage.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRemoveMessage(RemoveMessage removeMessage) throws TelegramApiException {
        try {
            executeCommand(DeleteMessage.builder()
                    .chatId(removeMessage.getChatId())
                    .messageId(removeMessage.getMessageId())
                    .build());
        } catch (TelegramApiException e) {
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
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendForwardMessage(long chatId, long fromChatId, int messageId) throws TelegramApiException {
        ForwardMessage forwardMessage = new ForwardMessage(String.valueOf(chatId), String.valueOf(fromChatId), messageId);
        forwardMessage.setProtectContent(true);
        try {
            executeCommand(forwardMessage);
        } catch (TelegramApiRequestException e) {
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

