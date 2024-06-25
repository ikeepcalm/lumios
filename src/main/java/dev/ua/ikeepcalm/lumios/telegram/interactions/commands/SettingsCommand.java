package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.SettingsMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@BotCommand(command = "settings")
public class SettingsCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText("""
                ≫ Налаштування ≪
                                
                В цьому меню ви зможете налаштувати роботу бота в цьому чаті. Натискайте на відповідні кнопки, щоб змінити налаштування!
                """);
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setReplyKeyboard(SettingsMarkupUtil.getSettingsKeyboard(chat));
        sendMessage(textMessage, message);
    }
}

