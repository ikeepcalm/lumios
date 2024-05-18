package dev.ua.ikeepcalm.lumios.telegram;

import dev.ua.ikeepcalm.lumios.telegram.wrappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramClient extends OkHttpTelegramClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramClient.class);

    public TelegramClient(@Value(value = "${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    private Object executeCommand(BotApiMethod<?> command) {
        try {
            return execute(command);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to execute {}", command.getClass(), e);
            throw new RuntimeException(e);
        }
    }

    public List<ChatMember> getChatAdministrators(String chatId) throws TelegramApiException {
        ArrayList<ChatMember> chats;
        chats = execute(new GetChatAdministrators(chatId));
        return chats;
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

    public Chat getChat(String chatId) throws TelegramApiException {
        return execute(new GetChat(chatId));
    }

    public File getFile(String fileId) {
        try {
            return execute(new GetFile(fileId));
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to get file", e);
            throw new RuntimeException(e);
        }
    }

    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        executeCommand(AnswerCallbackQuery.builder()
                .text(text)
                .callbackQueryId(callbackQueryId)
                .build());
    }

    public Message sendEditMessage(EditMessage editMessage) {
        return sendEditMessage(editMessage, false);
    }

    public Message sendEditMessage(EditMessage editMessage, boolean isCaption) {
        if (isCaption) {
            return (Message) executeCommand(EditMessageCaption.builder()
                    .messageId(editMessage.getMessageId())
                    .caption(editMessage.getText())
                    .parseMode(editMessage.getParseMode())
                    .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                    .chatId(editMessage.getChatId())
                    .build());
        } else {

            if (editMessage.getFilePath() == null && editMessage.getText() == null) {
                return (Message) executeCommand(EditMessageReplyMarkup.builder()
                        .messageId(editMessage.getMessageId())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            } else if (editMessage.getFilePath() == null) {
                return (Message) executeCommand(EditMessageText.builder()
                        .text(editMessage.getText())
                        .messageId(editMessage.getMessageId())
                        .chatId(editMessage.getChatId())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .build());
            } else {
                return (Message) executeCommand(EditMessageCaption.builder()
                        .messageId(editMessage.getMessageId())
                        .caption(editMessage.getText())
                        .parseMode(editMessage.getParseMode())
                        .replyMarkup((InlineKeyboardMarkup) editMessage.getReplyKeyboard())
                        .chatId(editMessage.getChatId())
                        .build());
            }
        }
    }

    public void pinChatMessage(long chatId, long messageId) {
        executeCommand(PinChatMessage.builder()
                .chatId(chatId)
                .messageId((int) messageId)
                .build());
    }

    public Message sendTextMessage(TextMessage textMessage) {
        return (Message) executeCommand(SendMessage.builder()
                .text(textMessage.getText())
                .chatId(textMessage.getChatId())
                .parseMode(textMessage.getParseMode())
                .replyMarkup(textMessage.getReplyKeyboard())
                .replyToMessageId(textMessage.getMessageId())
                .build());
    }

    public void sendRemoveMessage(RemoveMessage removeMessage) {
        executeCommand(DeleteMessage.builder()
                .chatId(removeMessage.getChatId())
                .messageId(removeMessage.getMessageId())
                .build());
    }

    public void sendReaction(ReactionMessage reactionMessage) {
        executeCommand(SetMessageReaction.builder()
                .chatId(reactionMessage.getChatId())
                .messageId(reactionMessage.getMessageId())
                .reactionTypes(reactionMessage.getReactionTypes())
                .build());
    }

    public void sendForwardMessage(long chatId, long fromChatId, int messageId) {
        ForwardMessage forwardMessage = new ForwardMessage(String.valueOf(chatId), String.valueOf(fromChatId), messageId);
        forwardMessage.setProtectContent(true);
        executeCommand(forwardMessage);
    }

    public void sendAnswerInlineQuery(AnswerInlineQuery answerInlineQuery) {
        executeCommand(answerInlineQuery);
    }
}

