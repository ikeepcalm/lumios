package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.SlotViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * A single time slot. {@code /now} and {@code /next} differ only in which slot they look for, so
 * everything else lives in {@link SlotViewRenderer}, which the scope callback shares.
 */
public abstract class SlotCommand extends ServicesShortcut implements Interaction {

    private final SlotViewRenderer renderer;

    protected SlotCommand(SlotViewRenderer renderer) {
        this.renderer = renderer;
    }

    protected abstract String commandType();

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        TextMessage answer = renderer.answer(message, chat, commandType());

        if ("private".equals(message.getChat().getType())) {
            telegramClient.sendTextMessage(answer);
            return;
        }
        sendMessage(answer, message);
    }
}
