package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;


@Component
@BotCommand(command = "help", aliases = ("start"))
public class HelpCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        if (message.getChat().getType().equals("private")) {
            String[] parts = message.getText().split(" ");
            if ((parts.length > 1 && parts[1].equals("help")) || parts[0].equals("/help")) {
                sendHelpMessage(message, chat);
            } else {
                String helloText = translationService.getMessage("help.private.hello", chat);
                TextMessage textMessage = new TextMessage();
                textMessage.setText(helloText);
                textMessage.setParseMode(MessageFormatter.getDefaultParseMode());
                textMessage.setChatId(message.getChatId());
                sendMessage(textMessage, message);
            }
        } else {
            String helpText = translationService.getMessage("help.group.hello", chat);
            TextMessage textMessage = new TextMessage();
            textMessage.setText(helpText);
            textMessage.setParseMode(ParseMode.MARKDOWN);
            textMessage.setChatId(message.getChatId());
            List<InlineKeyboardRow> keyboard = new ArrayList<>();
            InlineKeyboardRow firstRow = new InlineKeyboardRow();
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton website = new InlineKeyboardButton(translationService.getMessage("help.online_help", chat));
            InlineKeyboardButton pms = new InlineKeyboardButton(translationService.getMessage("help.short_help", chat));
            website.setUrl("https://www.lumios.dev/tutorial");
            pms.setUrl("https://t.me/lumios_bot?start=help");
            firstRow.add(website);
            secondRow.add(pms);
            keyboard.add(firstRow);
            keyboard.add(secondRow);
            textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
            sendMessage(textMessage, message);
        }
    }

    private void sendHelpMessage(Message message, LumiosChat chat) {
        String helpText = translationService.getMessage("help.full_guide", chat);
        sendMessage(helpText, ParseMode.MARKDOWN, message);
    }
}

