package dev.ua.ikeepcalm.merged.telegram.modules;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.*;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueLifecycleUtil;
import dev.ua.ikeepcalm.merged.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Timer;

@Component
public abstract class CallbackParent {

    protected Message message;
    protected AbsSender absSender;

    protected ReverenceChat reverenceChat;
    protected ReverenceUser reverenceUser;

    protected ChatService chatService;
    protected UserService userService;
    protected TaskService taskService;
    protected ShopService shopService;
    protected TimetableService timetableService;
    protected QueueLifecycleUtil queueLifecycleUtil;

    @Autowired
    private void setupDependencies(AbsSender absSender,
                                   ChatService chatService,
                                   UserService userService,
                                   TaskService taskService,
                                   ShopService shopService,
                                   TimetableService timetableService,
                                   QueueLifecycleUtil queueLifecycleUtil) {
        this.absSender = absSender;
        this.chatService = chatService;
        this.userService = userService;
        this.taskService = taskService;
        this.shopService = shopService;
        this.timetableService = timetableService;
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    protected void instantiateUpdate(CallbackQuery message) {
        this.message = message.getMessage();

        try {
            this.reverenceChat = chatService.findByChatId(message.getMessage().getChatId());
        } catch (NoSuchEntityException e) {
            ReverenceChat newChat = new ReverenceChat();
            newChat.setChatId(message.getMessage().getChatId());
            this.chatService.save(newChat);
            this.reverenceChat = newChat;
            sendMessage("""
                    Привіт!

                    Дякую за те, що додали мене сюди! Коротенький список того, що я вмію:
                                        
                    - створювати черги;
                    - записувати завдання;
                    - виводити розклад по команді;
                    - нагадувати про пару за декілька хвилин до початку
                    - керувати мікро-економікою всередині чату і ще багато чого!
                                        
                    Щоб дізнатися більше, натисніть /help@queueupnow_bot!
                    """);
            return;
        }

        try {
            this.reverenceUser = this.userService.findById(message.getFrom().getId(), reverenceChat);
        } catch (NoSuchEntityException e) {
            ReverenceUser newUser = new ReverenceUser();
            newUser.setUserId(message.getFrom().getId());
            newUser.setUsername(message.getFrom().getUserName());
            newUser.setCredits(100);
            newUser.setSustainable(100);
            newUser.setChannel(reverenceChat);
            userService.save(newUser);
            this.userService.save(newUser);
            this.reverenceUser = newUser;
            sendMessage("@" +  message.getFrom().getUserName() + """
                    Давай знайомитись! Мене звуть КуєуєАпБот, а тебе?
                                        
                    ...зроблю вигляд, що запам'ятав. Ще побачимося!
                    """);
        }
    }

    public abstract void processUpdate(CallbackQuery message);

    protected void sendMessage(String text) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setText(text);
        Message sent = absSender.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(String text, String parseMode) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setMessageId(message.getMessageId());
        textMessage.setParseMode(parseMode);
        textMessage.setText(text);
        Message sent = absSender.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void sendMessage(TextMessage textMessage) {
        Message sent = absSender.sendTextMessage(textMessage);
        scheduleMessageToDelete(message);
        scheduleMessageToDelete(sent);
    }

    protected void editMessage(EditMessage message){
        absSender.sendEditMessage(message);
    }

    private void scheduleMessageToDelete(Message message) {
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RemoveMessage removeMessage = new RemoveMessage(message.getMessageId(), message.getChatId());
                        absSender.sendRemoveMessage(removeMessage);
                    }
                }, 300000);
    }


}

