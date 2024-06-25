package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.ReactionMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@BotCommand(command = "task")
public class TaskCreationCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String text = message.getText().replace("@lumios_bot", "");
        String taskInfo = text.replace("/task", "").trim();
        String[] parts = taskInfo.split("\\s+");

        if (parts.length >= 3) {
            String dateStr = parts[0];
            String timeStr = parts[1];
            String taskName;
            String url = null;

            if (isValidURL(parts[parts.length - 1])) {
                taskName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length - 1));
                url = parts[parts.length - 1];
            } else {
                taskName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            }

            try {
                LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                LocalTime dueTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));

                DueTask task = new DueTask();
                task.setDueDate(dueDate);
                task.setDueTime(dueTime);
                task.setTaskName(taskName);
                task.setUrl(url);
                task.setChat(chat);
                taskService.save(task);
                ReactionMessage reactionMessage = new ReactionMessage();
                reactionMessage.setChatId(message.getChatId());
                reactionMessage.setMessageId(message.getMessageId());
                List<ReactionType> reactionTypes = new ArrayList<>();
                reactionTypes.add(new ReactionTypeEmoji(ReactionType.EMOJI_TYPE, "👾"));
                reactionMessage.setReactionTypes(reactionTypes);
                telegramClient.sendReaction(reactionMessage);
            } catch (DateTimeParseException e) {
                sendMessage("Неправильний формат дати або часу. Будь ласка, використовуйте формат HH:mm для часу, та dd.MM.yyyy", message);
            }
        } else {
            sendMessage("Неповна команда. Будь ласка, використовуйте /task dd.MM.yyyy HH:mm [Завдання] [Посилання]", message);
        }
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
