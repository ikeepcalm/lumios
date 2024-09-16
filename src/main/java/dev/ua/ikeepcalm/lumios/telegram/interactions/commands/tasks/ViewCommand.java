package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Collections;

@Component
@BotCommand(startsWith = "view_")
public class ViewCommand extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(ViewCommand.class);

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        String[] data = update.getMessage().getText().replace("@lumios_bot", "").split("_");
        long taskId = Long.parseLong(data[1]);
        DueTask task;
        try {
            task = taskService.findTaskById(chat.getChatId(), taskId);
        } catch (Exception e) {
            sendMessage("Завдання не знайдено. Схоже на серверну помилку, зверніться до підтримки!", update.getMessage());
            return;
        }

        if (task.getState() == TaskState.NOT_COMPLETED || task.getDueDate() == null) {
            sendMessage("Це завдання ще не налаштоване!", update.getMessage());
            return;
        }

        String message = """
                *%s*
                
                ◈ Опис:
                %s
                
                ◈ Дедлайн:
                %s %s
                
                ◈ Для кого:
                %s
                
                Створив: [ця людинка](tg://user?id=%d)
                
                """.formatted(
                task.getTaskName(),
                task.getDescription(),
                task.getDueDate().toString(),
                task.getDueTime().toString(),
                task.getScope() == null ? "Для всіх в цьому чаті" : task.getScope().getName(),
                task.getAuthor()
        );

        Message sent = null;
        if (task.getAttachment() != null) {
            try {
                sent = telegramClient.sendPhoto(String.valueOf(update.getMessage().getChatId()), task.getAttachment(), message);
            } catch (Exception e) {
                try {
                    sent = telegramClient.sendDocument(String.valueOf(update.getMessage().getChatId()), task.getAttachment(), message);
                } catch (Exception ex) {
                    log.error("Error while sending attachment", ex);
                }
            }
        } else {
            sent = sendMessage(message, ParseMode.MARKDOWN, update.getMessage());
        }

        if (task.getUrl() != null && sent != null) {
            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(update.getMessage().getChatId());
            editMessage.setMessageId(sent.getMessageId());
            InlineKeyboardRow row = new InlineKeyboardRow();
            InlineKeyboardButton buttons = new InlineKeyboardButton("Перейти до завдання");
            buttons.setUrl(task.getUrl());
            row.add(buttons);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(Collections.singletonList(row));
            editMessage.setReplyKeyboard(markup);
            telegramClient.sendEditMessage(editMessage);
        }
    }
}
