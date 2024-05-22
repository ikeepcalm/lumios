package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class ResetCommand extends CommandParent {

    private final ShotService shotService;

    public ResetCommand(ShotService shotService) {
        this.shotService = shotService;
    }

    @Override
    protected void processUpdate(Message message) {
        String[] parts = message.getText().split("\\s+", 2);
        LocalDate date;
        try {
            date = LocalDate.parse(parts[1]);
        } catch (DateTimeParseException e) {
            sendMessage("Неправильний формат дати. Спробуй ще раз. Наприклад: /reset 2021-12-31");
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage("Ти забув вказати дату. Спробуй ще раз. Наприклад: /reset 2021-12-31");
            return;
        }
        if (message.getFrom().getUserName().equals("ikeepcalm")) {
            sendMessage("Відновлення даних за датою " + date);
            ChatShot chatShot = null;
            try {
                chatShot = shotService.findByChatIdAndDate(reverenceChat.getChatId(), date);
            } catch (NoSuchEntityException e) {
                sendMessage("Дані за цю дату відсутні.");
                return;
            }
            for (UserShot user : chatShot.getUserShots()) {
                try {
                    ReverenceUser reverenceUser = userService.findById(user.getUserId(), reverenceChat);
                    reverenceUser.setReverence(user.getReverence());
                    userService.save(reverenceUser);
                } catch (NoSuchEntityException e) {
                    continue;
                }
            }
        } else {
            sendMessage("Ти не маєш прав на відновлення даних!");
        }
    }
}
