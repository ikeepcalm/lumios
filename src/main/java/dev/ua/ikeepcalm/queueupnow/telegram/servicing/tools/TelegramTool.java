package dev.ua.ikeepcalm.queueupnow.telegram.servicing.tools;

import dev.ua.ikeepcalm.queueupnow.telegram.servicing.TelegramService;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.PurgeMessage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Service
@PropertySource("classpath:thirdparty.properties")
public class TelegramTool extends DefaultAbsSender implements TelegramService {

    public TelegramTool(@Value("${telegram.bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    @Override
    public void sendAnswerCallbackQuery(String text, String callbackQueryId) {
        AnswerCallbackQuery acq = new AnswerCallbackQuery();
        acq.setText(text);
        acq.setShowAlert(true);
        acq.setCallbackQueryId(callbackQueryId);
        try {
            execute(acq);
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Couldn't send AnswerCallbackQuery: " + acq.toString());
        }
    }

    @Override
    public void sendForwardMessage(Message origin, long chatId){
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setMessageId(origin.getMessageId());
        forwardMessage.setChatId(chatId);
        forwardMessage.setFromChatId(origin.getChatId());
        forwardMessage.setProtectContent(true);
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Couldn't forward message: " + forwardMessage.toString());
        }
    }

    @Override
    public Message sendAlterMessage(AlterMessage alterMessage) {
        try {
            if (alterMessage.getFilePath() == null && alterMessage.getText() == null) {
                EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                editMessageReplyMarkup.setMessageId(alterMessage.getMessageId());
                editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) alterMessage.getReplyKeyboard());
                editMessageReplyMarkup.setChatId(alterMessage.getChatId());
                return (Message) execute(editMessageReplyMarkup);
            } else if (alterMessage.getFilePath() == null) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setText(alterMessage.getText());
                editMessageText.setMessageId(alterMessage.getMessageId());
                editMessageText.setChatId(alterMessage.getChatId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) alterMessage.getReplyKeyboard());
                return (Message) execute(editMessageText);
            } else {
                EditMessageCaption editMessageCaption = new EditMessageCaption();
                editMessageCaption.setMessageId(alterMessage.getMessageId());
                editMessageCaption.setCaption(alterMessage.getText());
                editMessageCaption.setParseMode(alterMessage.getParseMode());
                editMessageCaption.setReplyMarkup((InlineKeyboardMarkup) alterMessage.getReplyKeyboard());
                editMessageCaption.setChatId(alterMessage.getChatId());
                EditMessageMedia editMessageMedia = new EditMessageMedia();
                editMessageMedia.setMessageId(alterMessage.getMessageId());
                editMessageMedia.setChatId(alterMessage.getChatId());
                editMessageMedia.setMedia(new InputMediaPhoto(alterMessage.getFilePath()));
                execute(editMessageMedia);
                return (Message) execute(editMessageCaption);
            }
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Couldn't send AlterMessage message:" + alterMessage.toString());
        } return null;
    }

    @Override
    public void pinChatMessage(long chatId, long messageId){
        PinChatMessage pinChatMessage = new PinChatMessage();
        pinChatMessage.setChatId(chatId);
        pinChatMessage.setMessageId((int) messageId);
        try {
            execute(pinChatMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unpinChatMessage(long chatId, long messageId){
        UnpinChatMessage pinChatMessage = new UnpinChatMessage();
        pinChatMessage.setChatId(chatId);
        pinChatMessage.setMessageId((int) messageId);
        try {
            execute(pinChatMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sendMultiMessage(MultiMessage multiMessage) {
        try {
            if (multiMessage.getFilePath() != null) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setCaption(multiMessage.getText());
                sendPhoto.setChatId(multiMessage.getChatId());
                sendPhoto.setPhoto(new InputFile(multiMessage.getFilePath()));
                sendPhoto.setReplyMarkup(multiMessage.getReplyKeyboard());
                return execute(sendPhoto);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(multiMessage.getText());
                sendMessage.setChatId(multiMessage.getChatId());
                sendMessage.setReplyMarkup(multiMessage.getReplyKeyboard());
                return execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Failed to send MultiMessage: " + multiMessage.getText() + " / " + multiMessage.getChatId());
        }
        return null;
    }

    @Override
    public void sendPurgeMessage(PurgeMessage purgeMessage) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(purgeMessage.getChatId());
        deleteMessage.setMessageId(purgeMessage.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Couldn't delete message:" + purgeMessage.toString());
        }
    }
}
