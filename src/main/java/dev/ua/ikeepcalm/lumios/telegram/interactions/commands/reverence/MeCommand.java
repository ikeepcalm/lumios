package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@BotCommand(command = "me")
public class MeCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update message, LumiosUser user, LumiosChat chat) {
        if (user.getReverence() < 0) {
            sendMessage(translationService.getMessage("me.negative_reverence", chat, user.getReverence()), ParseMode.MARKDOWN
                    , message.getMessage());
        } else {
            sendMessage(translationService.getMessage("me.statistics", chat, user.getReverence(), user.getCredits(), user.getSustainable()), ParseMode.MARKDOWN, message.getMessage());
        }
    }
}