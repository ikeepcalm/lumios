package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

@Component
@BotCommand(command = "everyone", aliases = {"all"})
public class EveryoneCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        List<LumiosUser> users = userService.findAll(chat);
        String mentionText = users.stream()
                .map(LumiosUser::getUsername)
                .filter(username -> username != null && !username.isEmpty())
                .map(username -> "@" + username.replace("_", "\\_"))
                .collect(Collectors.joining(" "));

        if (mentionText.isEmpty()) {
            sendMessage("Нікого тегати, у всіх порожні юзернейми!", update.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(chat.getChatId());
        textMessage.setText(mentionText);
        textMessage.setParseMode(MessageFormatter.getDefaultParseMode());
        sendMessage(textMessage, update.getMessage());
    }
}
