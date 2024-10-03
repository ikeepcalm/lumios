package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@BotCommand(command = "unlock", aliases = {"freeslaves"})
public class UnlockCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        if (user.getUsername().equals("ikeepcalm")) {
            chat.setCommunicationLimit(300);
            chat.setSummaryLimit(10);
            chatService.save(chat);
            sendMessage("Ліміти на використання ШІ було знято!", update.getMessage());
        } else {
            sendMessage("Ви не можете виконувати цю команду!", update.getMessage());
        }
    }
}
