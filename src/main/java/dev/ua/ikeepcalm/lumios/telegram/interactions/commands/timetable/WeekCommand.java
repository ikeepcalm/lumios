package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@BotCommand(command = "week")
public class WeekCommand extends ServicesShortcut implements Interaction {

    private final WeekViewRenderer renderer;

    public WeekCommand(WeekViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        TextMessage answer = renderer.answer(message, chat);

        if ("private".equals(message.getChat().getType())) {
            telegramClient.sendTextMessage(answer);
            return;
        }
        sendMessage(answer, message);
    }
}
