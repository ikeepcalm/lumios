package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.CampusApiException;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportPicker;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil.CampusGroup;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@BotCommand(command = "import")
public class ImportCommand extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(ImportCommand.class);

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String[] parts = message.getText().split("\\s+", 2);
        String query = parts.length < 2 ? "" : parts[1].trim();

        if (query.isEmpty()) {
            sendMessage(translationService.getMessage("command.import.usage", chat), message);
            return;
        }
        if (query.length() < ImportUtil.MIN_QUERY_LENGTH) {
            sendMessage(translationService.getMessage("command.import.too-short", chat, String.valueOf(ImportUtil.MIN_QUERY_LENGTH)), message);
            return;
        }
        if (query.length() > ImportUtil.MAX_QUERY_LENGTH) {
            sendMessage(translationService.getMessage("command.import.too-long", chat, String.valueOf(ImportUtil.MAX_QUERY_LENGTH)), message);
            return;
        }

        List<CampusGroup> groups;
        try {
            groups = ImportUtil.searchGroups(query);
        } catch (CampusApiException e) {
            log.warn("KPI Campus group search failed for query '{}'", query, e);
            sendMessage(translationService.getMessage("command.import.api-error", chat), message);
            return;
        }

        if (groups.isEmpty()) {
            sendMessage(translationService.getMessage("command.import.not-found", chat, query), message);
            return;
        }

        TextMessage picker = new TextMessage();
        picker.setChatId(message.getChatId());
        picker.setMessageId(message.getMessageId());
        picker.setText(ImportPicker.text(translationService, chat, query, groups, 0));
        picker.setReplyKeyboard(ImportPicker.keyboard(translationService, chat, query, groups, 0));

        // Sent directly so the picker survives until the user picks or dismisses it; only the
        // command itself is cleaned up on the usual schedule.
        telegramClient.sendTextMessage(picker);
        scheduleMessageToDelete(message);
    }

}
