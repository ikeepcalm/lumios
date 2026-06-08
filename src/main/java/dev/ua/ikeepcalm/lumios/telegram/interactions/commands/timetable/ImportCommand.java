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
            sendMessage(translationService.getMessage("command.import.usage", chat), message);
            return;
        }
        String name = parts[1];

        if (name.isEmpty() || name.isBlank()) {
            sendMessage(translationService.getMessage("command.import.empty", chat), message);
            return;
        }

        Map<String, String> groups = ImportUtil.getGroupsByFilter(name);
        if (groups.isEmpty()) {
            sendMessage(translationService.getMessage("command.import.not-found", chat), message);
            return;
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(message.getChatId());
        textMessage.setText(translationService.getMessage("command.import.choose", chat));
        textMessage.setReplyKeyboard(ImportUtil.createGroupsKeyboard(groups));

        sendMessage(textMessage, message);
    }

}

