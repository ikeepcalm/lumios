package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.PagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@BotCommand(command = "stats")
public class StatsCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        List<LumiosUser> users = userService.findAll(chat);
        TextMessage message = new TextMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText(PagedUtil.buildStatsMessage(users, 1, translationService, chat));

        if (users.size() > 10) {
            message.setReplyKeyboard(buildStatsKeyboard());
        }

        sendMessage(message, update.getMessage());
    }

    private InlineKeyboardMarkup buildStatsKeyboard() {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton forward = new InlineKeyboardButton("➡️");
        forward.setCallbackData("stats-1-forward");
        firstRow.add(forward);
        keyboard.add(firstRow);
        return new InlineKeyboardMarkup(keyboard);
    }

}