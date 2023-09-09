/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.slf4j.spi.SLF4JServiceProvider
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.executing;

import dev.ua.ikeepcalm.merged.dal.impls.ChatServiceImpl;
import dev.ua.ikeepcalm.merged.dal.impls.RaiseServiceImpl;
import dev.ua.ikeepcalm.merged.dal.impls.UserServiceImpl;
import dev.ua.ikeepcalm.merged.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.dal.interfaces.RaiseService;
import dev.ua.ikeepcalm.merged.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.telegram.servicing.TelegramService;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public abstract class Executable {
    protected TelegramService telegramService;
    protected UserService userService;
    protected RaiseService raiseService;
    protected ChatService chatService;
    protected Logger logger;

    @Autowired
    private void init(TelegramService telegramService, UserServiceImpl userService, RaiseServiceImpl increasingService, ChatServiceImpl chatService) {
        this.telegramService = telegramService;
        this.userService = userService;
        this.raiseService = increasingService;
        this.chatService = chatService;
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }

    protected void reply(Message origin, String text) {
        MultiMessage message = new MultiMessage();
        message.setMessageId(origin.getMessageId());
        message.setChatId(origin.getChatId());
        message.setText(text);
        this.telegramService.sendMultiMessage(message);
    }

    protected void sendMessage(Message origin, String text) {
        MultiMessage message = new MultiMessage();
        message.setChatId(origin.getChatId());
        message.setText(text);
        this.telegramService.sendMultiMessage(message);
    }

    protected void sendAndEditMessage(long linkedChatId, String sendText, String editText, int timeout) {
        MultiMessage message = new MultiMessage();
        message.setChatId(linkedChatId);
        message.setText(sendText);
        Message sentMessage = this.telegramService.sendMultiMessage(message);
        try {
            Thread.sleep(timeout);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        AlterMessage editedMessage = new AlterMessage();
        editedMessage.setChatId(linkedChatId);
        editedMessage.setMessageId(sentMessage.getMessageId());
        editedMessage.setText(editText);
        this.telegramService.sendAlterMessage(editedMessage);
    }

    protected void sendCallbackMessage(CallbackQuery origin, String text) {
        this.telegramService.sendCallbackMessage(origin, text);
    }

    protected void deleteCallbackMessage(CallbackQuery origin) {
        this.telegramService.deleteCallbackMessage(origin);
    }
}

