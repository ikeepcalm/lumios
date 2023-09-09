/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.PropertySource
 *  org.springframework.stereotype.Service
 *  org.telegram.telegrambots.bots.DefaultAbsSender
 *  org.telegram.telegrambots.bots.DefaultBotOptions
 *  org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
 *  org.telegram.telegrambots.meta.api.methods.BotApiMethod
 *  org.telegram.telegrambots.meta.api.methods.ForwardMessage
 *  org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage
 *  org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage
 *  org.telegram.telegrambots.meta.api.methods.send.SendMessage
 *  org.telegram.telegrambots.meta.api.methods.send.SendPhoto
 *  org.telegram.telegrambots.meta.api.methods.send.SendVideo
 *  org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
 *  org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption
 *  org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
 *  org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
 *  org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 *  org.telegram.telegrambots.meta.api.objects.InputFile
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.media.InputMedia
 *  org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
 *  org.telegram.telegrambots.meta.exceptions.TelegramApiException
 */
package dev.ua.ikeepcalm.merged.telegram.servicing.tools;

import dev.ua.ikeepcalm.merged.telegram.servicing.TelegramService;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.PurgeMessage;
import java.io.File;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@PropertySource(value={"classpath:thirdparty.properties"})
public class TelegramTool
extends DefaultAbsSender
implements TelegramService {
    public TelegramTool(@Value(value="${telegram.bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    @Override
    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        AnswerCallbackQuery acq = new AnswerCallbackQuery();
        acq.setText(text);
        acq.setShowAlert(Boolean.valueOf(true));
        acq.setCallbackQueryId(callbackQueryId);
        try {
            this.execute((BotApiMethod)acq);
        }
        catch (TelegramApiException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't send AnswerCallbackQuery: " + acq);
        }
    }

    @Override
    public void sendForwardMessage(Message origin, long chatId) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setMessageId(origin.getMessageId());
        forwardMessage.setChatId(Long.valueOf(chatId));
        forwardMessage.setFromChatId(origin.getChatId());
        forwardMessage.setProtectContent(Boolean.valueOf(true));
        try {
            this.execute((BotApiMethod)forwardMessage);
        }
        catch (TelegramApiException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't forward message: " + forwardMessage);
        }
    }

    @Override
    public Message sendAlterMessage(AlterMessage alterMessage) {
        try {
            if (alterMessage.getFilePath() == null && alterMessage.getText() == null) {
                EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                editMessageReplyMarkup.setMessageId(Integer.valueOf(alterMessage.getMessageId()));
                editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup)alterMessage.getReplyKeyboard());
                editMessageReplyMarkup.setChatId(alterMessage.getChatId());
                return (Message)this.execute((BotApiMethod)editMessageReplyMarkup);
            }
            if (alterMessage.getFilePath() == null) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setText(alterMessage.getText());
                editMessageText.setMessageId(Integer.valueOf(alterMessage.getMessageId()));
                editMessageText.setChatId(alterMessage.getChatId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup)alterMessage.getReplyKeyboard());
                return (Message)this.execute((BotApiMethod)editMessageText);
            }
            EditMessageCaption editMessageCaption = new EditMessageCaption();
            editMessageCaption.setMessageId(Integer.valueOf(alterMessage.getMessageId()));
            editMessageCaption.setCaption(alterMessage.getText());
            editMessageCaption.setParseMode(alterMessage.getParseMode());
            editMessageCaption.setReplyMarkup((InlineKeyboardMarkup)alterMessage.getReplyKeyboard());
            editMessageCaption.setChatId(alterMessage.getChatId());
            EditMessageMedia editMessageMedia = new EditMessageMedia();
            editMessageMedia.setMessageId(Integer.valueOf(alterMessage.getMessageId()));
            editMessageMedia.setChatId(alterMessage.getChatId());
            editMessageMedia.setMedia((InputMedia)new InputMediaPhoto(alterMessage.getFilePath()));
            this.execute(editMessageMedia);
            return (Message)this.execute((BotApiMethod)editMessageCaption);
        }
        catch (TelegramApiException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't send AlterMessage message:" + alterMessage);
            return null;
        }
    }

    @Override
    public void pinChatMessage(long chatId, long messageId) {
        PinChatMessage pinChatMessage = new PinChatMessage();
        pinChatMessage.setChatId(Long.valueOf(chatId));
        pinChatMessage.setMessageId(Integer.valueOf((int)messageId));
        try {
            this.execute((BotApiMethod)pinChatMessage);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unpinChatMessage(long chatId, long messageId) {
        UnpinChatMessage pinChatMessage = new UnpinChatMessage();
        pinChatMessage.setChatId(Long.valueOf(chatId));
        pinChatMessage.setMessageId(Integer.valueOf((int)messageId));
        try {
            this.execute((BotApiMethod)pinChatMessage);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InlineKeyboardMarkup createMarkup(String[] values, String prefix) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        ArrayList markupList = new ArrayList();
        ArrayList<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        for (String name : values) {
            String callbackOfButton = prefix + name;
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setCallbackData(callbackOfButton);
            button.setText(name);
            buttons.add(button);
        }
        markupList.add(buttons);
        inlineKeyboardMarkup.setKeyboard(markupList);
        return inlineKeyboardMarkup;
    }

    @Override
    public void sendVideo(long chatId, String path, int messageId) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(Long.valueOf(chatId));
        sendVideo.setVideo(new InputFile(new File(path)));
        sendVideo.setReplyToMessageId(Integer.valueOf(messageId));
        try {
            this.execute(sendVideo);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCallbackMessage(CallbackQuery origin) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(origin.getMessage().getChatId()));
        deleteMessage.setMessageId(origin.getMessage().getMessageId());
        try {
            this.execute((BotApiMethod)deleteMessage);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendCallbackMessage(CallbackQuery origin, String messageText) {
        SendMessage message = new SendMessage();
        message.setText(messageText);
        message.setChatId(origin.getMessage().getChatId());
        try {
            this.execute((BotApiMethod)message);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sendMultiMessage(MultiMessage multiMessage) {
        try {
            if (multiMessage.getFilePath() != null) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setCaption(multiMessage.getText());
                sendPhoto.setChatId(Long.valueOf(multiMessage.getChatId()));
                sendPhoto.setPhoto(new InputFile(multiMessage.getFilePath()));
                sendPhoto.setReplyMarkup(multiMessage.getReplyKeyboard());
                return this.execute(sendPhoto);
            }
            if (multiMessage.getMessageId() != 0) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(multiMessage.getText());
                sendMessage.setChatId(Long.valueOf(multiMessage.getChatId()));
                sendMessage.setReplyToMessageId(Integer.valueOf(multiMessage.getMessageId()));
                sendMessage.setReplyMarkup(multiMessage.getReplyKeyboard());
                return (Message)this.execute((BotApiMethod)sendMessage);
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(multiMessage.getText());
            sendMessage.setChatId(Long.valueOf(multiMessage.getChatId()));
            sendMessage.setReplyMarkup(multiMessage.getReplyKeyboard());
            return (Message)this.execute((BotApiMethod)sendMessage);
        }
        catch (TelegramApiException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Failed to send MultiMessage: " + multiMessage.getText() + " / " + multiMessage.getChatId());
            return null;
        }
    }

    @Override
    public void sendPurgeMessage(PurgeMessage purgeMessage) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(Long.valueOf(purgeMessage.getChatId()));
        deleteMessage.setMessageId(Integer.valueOf(purgeMessage.getMessageId()));
        try {
            this.execute((BotApiMethod)deleteMessage);
        }
        catch (TelegramApiException e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't delete message:" + purgeMessage);
        }
    }
}

