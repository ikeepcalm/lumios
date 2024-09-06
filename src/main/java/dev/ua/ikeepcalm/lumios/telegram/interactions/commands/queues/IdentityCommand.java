package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@BotCommand(command = "identity")
public class IdentityCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        String text = update.getMessage().getText();
        String[] split = text.split(" ", 2);

        if (split.length == 1) {
            sendMessage("Помилка! Введіть після команди своє ПІБ, щоб я зміг прив'язати його до вас!", update.getMessage());
            return;
        }

        user.setFullName(split[1]);
        userService.save(user);

        sendMessage("Ваше ПІБ успішно встановлене на " + split[1], update.getMessage());
    }

}
