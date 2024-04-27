package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;


@Component
public class InfoCommand extends CommandParent {

    private final ChatService chatService;

    public InfoCommand(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void processUpdate(Message message) {
        Message reply = message.getReplyToMessage();
        if (!message.getFrom().getUserName().equals("ikeepcalm")) {
            return;
        }

        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats) {
            sendForwardMessage(chat.getChatId(), message.getChatId(), reply.getMessageId());
        }
    }
}

