package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport.Target;
import dev.ua.ikeepcalm.lumios.telegram.utils.WorkloadReporter;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * {@code /due} - what the caller probably has to get done over the next fortnight, worked out from
 * their own timetable.
 * <p>
 * Resolved like the other timetable views, so it answers in a private chat too, and asks which group
 * is meant when the member is in several.
 */
@Component
@BotCommand(command = "due")
public class DueCommand extends ServicesShortcut implements Interaction {

    private final TimetableViewSupport viewSupport;
    private final WorkloadReporter reporter;

    public DueCommand(TimetableViewSupport viewSupport, WorkloadReporter reporter) {
        this.viewSupport = viewSupport;
        this.reporter = reporter;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        Long telegramUserId = message.getFrom().getId();
        Target target = viewSupport.target(message, chat, telegramUserId);

        if (target.ambiguous()) {
            TextMessage question = new TextMessage();
            question.setChatId(message.getChatId());
            question.setMessageId(message.getMessageId());
            question.setText(translationService.getMessage("view.choose-group", chat));
            question.setReplyKeyboard(viewSupport.chooseGroupKeyboard(target.chooseFrom(), "due"));
            sendMessage(question, message);
            return;
        }
        if (!target.resolved()) {
            sendMessage(translationService.getMessage("mine.no-groups", chat), message);
            return;
        }

        reporter.report(message.getChatId(), target.groupChat(), chat, telegramUserId);
    }
}
