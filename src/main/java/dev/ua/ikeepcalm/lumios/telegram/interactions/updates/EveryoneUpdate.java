package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

@Component
@BotUpdate
public class EveryoneUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText().toLowerCase();
        if (text.contains("@everyone") || text.contains("@all")) {
            try {
                LumiosChat chat = chatService.findByChatId(update.getMessage().getChatId());
                List<LumiosUser> users = userService.findAll(chat);
                String mentionText = users.stream()
                        .map(LumiosUser::getUsername)
                        .filter(username -> username != null && !username.isEmpty())
                        .map(username -> "@" + username.replace("_", "\\_"))
                        .collect(Collectors.joining(" "));

                if (mentionText.isEmpty()) {
                    return;
                }

                TextMessage textMessage = new TextMessage();
                textMessage.setChatId(chat.getChatId());
                textMessage.setText(mentionText);
                textMessage.setParseMode(MessageFormatter.getDefaultParseMode());
                sendMessage(textMessage, update.getMessage());
            } catch (NoSuchEntityException ignored) {
            }
        }
    }
}
