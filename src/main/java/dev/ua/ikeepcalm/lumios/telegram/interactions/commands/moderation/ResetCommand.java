package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.moderation;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@BotCommand(command = "reset")
public class ResetCommand extends ServicesShortcut implements Interaction {

    private final ShotService shotService;

    public ResetCommand(ShotService shotService) {
        this.shotService = shotService;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String[] parts = message.getText().split("\\s+", 2);
        LocalDate date;
        try {
            date = LocalDate.parse(parts[1]);
        } catch (DateTimeParseException e) {
            sendMessage(translationService.getMessage("command.reset.invalid_date", chat), message);
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage(translationService.getMessage("command.reset.no_date", chat), message);
            return;
        }
        if (message.getFrom().getUserName().equals("ikeepcalm")) {
            sendMessage(translationService.getMessage("command.reset.restoring", chat, date), message);
            ChatShot chatShot;
            try {
                chatShot = shotService.findByChatIdAndDate(chat.getChatId(), date);
            } catch (NoSuchEntityException e) {
                sendMessage(translationService.getMessage("command.reset.no_data", chat), message);
                return;
            }
            for (UserShot u : chatShot.getUserShots()) {
                try {
                    LumiosUser lumiosUser = userService.findById(u.getUserId(), chat);
                    lumiosUser.setReverence(u.getReverence());
                    userService.save(lumiosUser);
                } catch (NoSuchEntityException ignored) {
                }
            }
        } else {
            sendMessage(translationService.getMessage("command.reset.no_permission", chat), message);
        }
    }
}
