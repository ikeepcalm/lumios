package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.DayViewRenderer;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;

/**
 * One day of the timetable. {@code /today} and {@code /tomorrow} differ only in which date they ask
 * for, so everything else - resolving the group, filtering to the caller's own classes, paging - lives
 * in {@link DayViewRenderer}, which the paging callback shares.
 */
public abstract class DayCommand extends ServicesShortcut implements Interaction {

    private final DayViewRenderer renderer;

    protected DayCommand(DayViewRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Matches the {@code command.<type>.title} / {@code .no-classes} / {@code .not-found} translation
     * keys and the paging payload.
     */
    protected abstract String commandType();

    protected abstract LocalDate date();

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        TextMessage answer = renderer.answer(message, chat, commandType(), date());

        if ("private".equals(message.getChat().getType())) {
            // Nothing to tidy up in a one-to-one chat, and a timetable the member asked for should not
            // vanish from under them five minutes later.
            telegramClient.sendTextMessage(answer);
            return;
        }
        sendMessage(answer, message);
    }
}
