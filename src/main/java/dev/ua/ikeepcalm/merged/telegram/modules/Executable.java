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
package dev.ua.ikeepcalm.merged.telegram.modules;

import dev.ua.ikeepcalm.merged.database.dal.impls.ChatServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.impls.ShopServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.impls.TaskServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ShopService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public abstract class Executable {
    protected AbsSender absSender;
    protected UserService userService;
    protected ShopService shopService;
    protected ChatService chatService;
    protected TaskService taskService;
    protected Logger logger;

    @Autowired
    private void init(TaskServiceImpl taskService, AbsSender absSender, UserService userService, ShopServiceImpl increasingService, ChatServiceImpl chatService) {
        this.userService = userService;
        this.shopService = increasingService;
        this.chatService = chatService;
        this.taskService = taskService;
        this.absSender = absSender;
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }

    protected Message reply(Message origin, String text) {
        TextMessage message = new TextMessage();
        message.setMessageId(origin.getMessageId());
        message.setChatId(origin.getChatId());
        message.setText(text);
        return absSender.sendTextMessage(message);
    }

    protected void sendMessage(Message origin, String text) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setText(text);
        absSender.sendTextMessage(message);
    }

    protected void sendAndEditMessage(long linkedChatId, String sendText, String editText) {
        TextMessage message = new TextMessage();
        message.setChatId(linkedChatId);
        message.setText(sendText);
        Message sentMessage = this.absSender.sendTextMessage(message);
        try {
            Thread.sleep(12000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        EditMessage editedMessage = new EditMessage();
        editedMessage.setChatId(linkedChatId);
        editedMessage.setMessageId(sentMessage.getMessageId());
        editedMessage.setText(editText);
        this.absSender.sendEditMessage(editedMessage);
    }

    protected void sendCallbackMessage(CallbackQuery origin, String text) {
        this.absSender.sendCallbackMessage(origin, text);
    }

    protected void removeCallbackMessage(CallbackQuery origin) {
        this.absSender.removeCallbackMessage(origin);
    }
}

