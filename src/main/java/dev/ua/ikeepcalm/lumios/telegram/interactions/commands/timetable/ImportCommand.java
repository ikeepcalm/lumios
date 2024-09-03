package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;

@Component
@BotCommand(command = "import")
public class ImportCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String[] parts = message.getText().split("\\s+", 2);
        if (parts.length < 2) {
            sendMessage("Напиши назву групи, яку хочеш імпортувати. Наприклад: /import ІП-32", message);
            return;
        }
        String name = parts[1];

        if (name.isEmpty() || name.isBlank()) {
            sendMessage("Назва не може бути пустою. Спробуй ще раз. Наприклад: /import ІП-32", message);
            return;
        }

        Map<String, String> groups = ImportUtil.getGroupsByFilter(name);
        if (groups.isEmpty()) {
            sendMessage("Не знайдено жодної групи за таким фільтром. Спробуй ще раз.", message);
            return;
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText("Обери групу, яку хочеш імпортувати:\n\nЯкщо ти не знайшов свою групу, спробуй ввести інший фільтр.");
        textMessage.setReplyKeyboard(ImportUtil.createGroupsKeyboard(groups));

        sendMessage(textMessage, message);
    }

}

