package dev.ua.ikeepcalm.merged.telegram.modules;

import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Timer;

@Component
public abstract class CommandParent {
    protected AbsSender absSender;
    protected Logger logger;

    @Autowired
    private void init(AbsSender absSender) {
        this.absSender = absSender;
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }

    protected void sendMessage(Message origin, String text){
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin);
        scheduleMessageToDelete(sent);
    }


    protected void sendMessage(CallbackQuery origin, String text){
        TextMessage message = new TextMessage();
        message.setChatId(origin.getMessage().getChatId());
        message.setMessageId(origin.getMessage().getMessageId());
        message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin.getMessage());
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(Message origin, TextMessage message){
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin);
        scheduleMessageToDelete(sent);
    }

    protected void reply(Message origin, String text) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setMessageId(origin.getMessageId());message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin);
        scheduleMessageToDelete(sent);
    }

    protected void reply(Message origin, String parseMode, String text) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setParseMode(parseMode);
        message.setMessageId(origin.getMessageId());
        message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin);
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(Message origin, String parseMode, String text){
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setMessageId(origin.getMessageId());
        message.setText(text);
        message.setParseMode(parseMode);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin);
        scheduleMessageToDelete(sent);
    }

    protected void scheduleMessageToDelete(Message origin) {
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RemoveMessage removeMessage = new RemoveMessage(origin.getMessageId(), origin.getChatId());
                        absSender.sendRemoveMessage(removeMessage);
                    }
                }, 300000);
    }
}

