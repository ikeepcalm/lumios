package dev.ua.ikeepcalm.merged.telegram.modules;

import dev.ua.ikeepcalm.merged.database.dal.impls.ChatServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.impls.ShopServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.impls.TaskServiceImpl;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.ShopService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Timer;

@Component
public abstract class CommandParent {
    protected AbsSender absSender;
    protected UserService userService;
    protected ShopService shopService;
    protected ChatService chatService;
    protected TaskService taskService;
    protected Timer timer;
    protected Logger logger;

    @Autowired
    private void init(TaskServiceImpl taskService, AbsSender absSender, UserService userService, ShopServiceImpl increasingService, ChatServiceImpl chatService) {
        this.userService = userService;
        this.shopService = increasingService;
        this.chatService = chatService;
        this.taskService = taskService;
        this.absSender = absSender;
        this.timer = new Timer();
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }

    protected void reply(Message origin, String text) {
        TextMessage message = new TextMessage();
        message.setMessageId(origin.getMessageId());
        message.setChatId(origin.getChatId());
        message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin, sent);
    }

    protected Message sendMessage(Message origin, String text) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setText(text);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin, sent);
        return sent;
    }

    protected void sendMessage(Message origin, String text, boolean enableParseMarkup) {
        TextMessage message = new TextMessage();
        message.setChatId(origin.getChatId());
        message.setText(text);
        message.setEnableParseMode(enableParseMarkup);
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin, sent);
    }

    protected void sendMessage(Message origin, TextMessage message) {
        Message sent = absSender.sendTextMessage(message);
        scheduleMessageToDelete(origin, sent);
    }

    protected void sendCallbackMessage(CallbackQuery origin, String text) {
        Message sent = absSender.sendCallbackMessage(origin, text);
        scheduleMessageToDelete(origin.getMessage(), sent);
    }

    protected void removeCallbackMessage(CallbackQuery origin) {
        absSender.removeCallbackMessage(origin);
    }


    protected void scheduleMessageToDelete(Message origin, Message sent) {
        timer.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RemoveMessage purgeAction = new RemoveMessage(origin.getMessageId(), origin.getChatId());
                        RemoveMessage purgeResponse = new RemoveMessage(sent.getMessageId(), sent.getChatId());
                        absSender.sendRemoveMessage(purgeResponse);
                        absSender.sendRemoveMessage(purgeAction);
                    }
                }, 300000);
    }
}

