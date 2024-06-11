package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.commands;

import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.utils.SettingsMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class SettingsCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText("""
                ≫ Налаштування ≪
                
                В цьому меню ви зможете налаштувати роботу бота в цьому чаті. Натискайте на відповідні кнопки, щоб змінити налаштування!
                """);
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setReplyKeyboard(SettingsMarkupUtil.getSettingsKeyboard(reverenceChat));
        telegramClient.sendTextMessage(textMessage);
    }

}

