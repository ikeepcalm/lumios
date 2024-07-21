package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.SettingsMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@BotCallback(startsWith = "settings-")
public class SettingsCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String data = message.getData();
        if (data.equals("settings-timetable-enable")) {
            chat.setTimetableEnabled(true);
        } else if (data.equals("settings-timetable-disable")) {
            chat.setTimetableEnabled(false);
        }

        if (data.equals("settings-dice-enable")) {
            chat.setDiceEnabled(true);
        } else if (data.equals("settings-dice-disable")) {
            chat.setDiceEnabled(false);
        }
        chatService.save(chat);


        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getMessage().getChatId());
        editMessage.setMessageId(message.getMessage().getMessageId());
        editMessage.setText("""
                ≫ Налаштування ≪
                                
                В цьому меню ви зможете налаштувати роботу бота в цьому чаті. Натискайте на відповідні кнопки, щоб змінити налаштування!
                """);
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage.setReplyKeyboard(SettingsMarkupUtil.getSettingsKeyboard(chat));
        editMessage(editMessage);
    }
}

