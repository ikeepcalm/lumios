package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.moderation;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.DebugUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.Map;

@Component
@BotCommand(command = "debug")
public class DebugCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        if (message.getFrom().getUserName().equals("ikeepcalm")) {
            Iterable<LumiosChat> chats = chatService.findAll();
            Map<String, String> groups = new HashMap<>();
            for (LumiosChat lumiosChat : chats) {
                String name = lumiosChat.getName();
                if (name == null) {
                    name = "Unnamed";
                }
                String id = lumiosChat.getChatId().toString();
                if (id.startsWith("-")) {
                    groups.put(id, name);
                }
            }

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText("<< DEBUG MENU >>");
            textMessage.setReplyKeyboard(DebugUtil.createGroupsKeyboard(groups));

            sendMessage(textMessage, message);
        }
    }
}