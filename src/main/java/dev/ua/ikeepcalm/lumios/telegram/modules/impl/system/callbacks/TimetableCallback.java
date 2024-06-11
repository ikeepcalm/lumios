package dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.callbacks;

import dev.ua.ikeepcalm.lumios.telegram.modules.impl.system.utils.SettingsMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class TimetableCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String data = message.getData();
        if (data.equals("settings-timetable-enable")) {
            reverenceChat.setTimetableEnabled(true);
        } else if (data.equals("settings-timetable-disable")) {
            reverenceChat.setTimetableEnabled(false);
        }
        chatService.save(reverenceChat);

        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getMessage().getChatId());
        editMessage.setMessageId(message.getMessage().getMessageId());
        editMessage.setText("""
                ≫ Налаштування ≪
                
                В цьому меню ви зможете налаштувати роботу бота в цьому чаті. Натискайте на відповідні кнопки, щоб змінити налаштування!
                """);
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage.setReplyKeyboard(SettingsMarkupUtil.getSettingsKeyboard(reverenceChat));
        editMessage(editMessage);
    }

}

