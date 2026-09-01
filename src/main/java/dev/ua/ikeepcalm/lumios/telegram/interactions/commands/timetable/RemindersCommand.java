package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.ReminderSettingsPicker;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * {@code /reminders} - where a member says how they want to be told about their classes: privately, by
 * a mention on the group announcement, both, or not at all, plus the advance warning and the morning
 * digest.
 * <p>
 * Settings are per group, because a member of two groups may well want different things from each. The
 * menu itself only makes sense privately, so invoked in a group the command just offers to open it.
 */
@Component
@BotCommand(command = "reminders", aliases = {"notify"})
public class RemindersCommand extends ServicesShortcut implements Interaction {

    private final PersonalTimetableSupport support;

    public RemindersCommand(PersonalTimetableSupport support) {
        this.support = support;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        Long telegramUserId = message.getFrom().getId();

        if (!"private".equals(message.getChat().getType())) {
            offerPrivateMenu(message, chat);
            return;
        }

        List<LumiosChat> groups = support.timetabledGroupsOf(telegramUserId);
        if (groups.isEmpty()) {
            sendMessage(translationService.getMessage("mine.no-groups", chat), message);
            return;
        }
        if (groups.size() > 1) {
            sendMessage(chooseGroup(chat, groups), message);
            return;
        }

        support.sendReminderMenu(groups.getFirst(), telegramUserId, message.getChatId(), null);
    }

    private void offerPrivateMenu(Message message, LumiosChat chat) {
        TextMessage prompt = new TextMessage();
        prompt.setChatId(message.getChatId());
        prompt.setMessageId(message.getMessageId());
        prompt.setText(translationService.getMessage("reminders.group.prompt", chat));

        InlineKeyboardButton open = new InlineKeyboardButton(
                translationService.getMessage("reminders.button.open", chat));
        open.setCallbackData(ReminderSettingsPicker.FROM_GROUP + chat.getChatId());
        prompt.setReplyKeyboard(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(open))));

        sendMessage(prompt, message);
    }

    private TextMessage chooseGroup(LumiosChat privateChat, List<LumiosChat> groups) {
        List<InlineKeyboardRow> keyboard = groups.stream()
                .map(group -> {
                    InlineKeyboardButton button = new InlineKeyboardButton(
                            group.getName() == null ? String.valueOf(group.getChatId()) : group.getName());
                    button.setCallbackData(ReminderSettingsPicker.CHOOSE_CHAT + group.getChatId());
                    return new InlineKeyboardRow(button);
                })
                .toList();

        TextMessage message = new TextMessage();
        message.setChatId(privateChat.getChatId());
        message.setText(translationService.getMessage("mine.choose-group", privateChat));
        message.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        return message;
    }
}
