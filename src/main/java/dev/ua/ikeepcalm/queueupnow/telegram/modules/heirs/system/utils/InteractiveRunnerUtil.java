package dev.ua.ikeepcalm.queueupnow.telegram.modules.system.utils;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.telegram.TelegramClient;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class InteractiveRunnerUtil {

    private final TelegramClient telegramClient;
    private final ChatService chatService;

    public InteractiveRunnerUtil(TelegramClient telegramClient,
                                 ChatService chatService) {
        this.telegramClient = telegramClient;
        this.chatService = chatService;
    }

    public void ip32Command(String argument) {
        TextMessage textMessage = new TextMessage();
        textMessage.setText(argument);
        textMessage.setChatId(-1001767321866L);
        telegramClient.sendTextMessage(textMessage);
    }

    public void announceCommand(String argument) {
        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats) {
            TextMessage textMessage = new TextMessage();
            textMessage.setText(argument);
            textMessage.setChatId(chat.getChatId());
            telegramClient.sendTextMessage(textMessage);
        }
    }
}
