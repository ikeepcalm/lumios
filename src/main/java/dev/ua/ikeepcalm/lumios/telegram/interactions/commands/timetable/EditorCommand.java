package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@BotCommand(command = "editor")
public class EditorCommand extends ServicesShortcut implements Interaction {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.miniapp.name}")
    private String miniAppName;

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText(translationService.getMessage("command.editor.text", chat));
        textMessage.setMessageId(message.getMessageId());

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton notify = new InlineKeyboardButton(translationService.getMessage("command.editor.button", chat));
        notify.setUrl(miniAppLink(message.getChatId()));
        firstRow.add(notify);
        keyboard.add(firstRow);
        textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
        sendMessage(textMessage, message);
    }

    /**
     * A direct Mini App link rather than a {@code web_app} button: Telegram rejects those outside
     * private chats, so {@code startapp} is the only channel that carries the group id into the app.
     * <p>
     * A private chat has no timetable of its own, so it gets the app without a group to open - the
     * alternative would be a link that always fails to load.
     */
    private String miniAppLink(long chatId) {
        String username = botUsername.startsWith("@") ? botUsername.substring(1) : botUsername;
        String link = "https://t.me/%s/%s".formatted(username, miniAppName);
        return chatId < 0 ? link + "?startapp=" + chatId : link;
    }
}

