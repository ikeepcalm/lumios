package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectivePicker;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * {@code /mine} - lets a member say which electives they attend, so their reminders stop announcing
 * the other five options in every slot.
 * <p>
 * The menu itself only makes sense privately: it is personal, and a private chat is also proof the
 * bot can reach them. Invoked in a group, the command therefore just offers to open it.
 */
@Component
@BotCommand(command = "mine", aliases = {"electives"})
public class MineCommand extends ServicesShortcut implements Interaction {

    private final PersonalTimetableSupport support;

    public MineCommand(PersonalTimetableSupport support) {
        this.support = support;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        Long telegramUserId = message.getFrom().getId();

        if (!"private".equals(message.getChat().getType())) {
            offerPrivateMenu(message, chat, telegramUserId);
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

        support.sendMenu(groups.getFirst(), telegramUserId, message.getChatId(), null);
    }

    /**
     * In a group the command answers with a button rather than the menu, so nobody's choices end up
     * in the shared chat.
     */
    private void offerPrivateMenu(Message message, LumiosChat chat, Long telegramUserId) {
        TextMessage prompt = new TextMessage();
        prompt.setChatId(message.getChatId());
        prompt.setMessageId(message.getMessageId());
        prompt.setText(translationService.getMessage("mine.group.prompt", chat));

        InlineKeyboardButton open = new InlineKeyboardButton(translationService.getMessage("mine.button.open", chat));
        open.setCallbackData(ElectivePicker.FROM_GROUP + chat.getChatId());
        prompt.setReplyKeyboard(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(open))));

        sendMessage(prompt, message);
    }

    private TextMessage chooseGroup(LumiosChat privateChat, List<LumiosChat> groups) {
        List<InlineKeyboardRow> keyboard = groups.stream()
                .map(group -> {
                    InlineKeyboardButton button = new InlineKeyboardButton(
                            group.getName() == null ? String.valueOf(group.getChatId()) : group.getName());
                    button.setCallbackData(ElectivePicker.CHOOSE_CHAT + group.getChatId());
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
