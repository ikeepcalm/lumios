package dev.ua.ikeepcalm.merged.telegram.modules.system.utils;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.lifecycles.SimpleQueueLifecycle;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class InteractiveRunnerUtil {

    private final AbsSender absSender;
    private final ChatService chatService;

    public InteractiveRunnerUtil(AbsSender absSender,
                                 ChatService chatService) {
        this.absSender = absSender;
        this.chatService = chatService;
    }

    public void ip32Command(String argument){
        TextMessage textMessage = new TextMessage();
        textMessage.setText(argument);
        textMessage.setChatId(-1001767321866L);
        absSender.sendTextMessage(textMessage);
    }

    public void announceCommand(String argument){
        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats){
            TextMessage textMessage = new TextMessage();
            textMessage.setText(argument);
            textMessage.setChatId(chat.getChatId());
            absSender.sendTextMessage(textMessage);
        }
    }
}
