package dev.ua.ikeepcalm.merged.telegram.modules.tasks.commands;

import dev.ua.ikeepcalm.merged.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Component
public class TaskCreationCommand extends CommandParent {
    public void execute(Message origin) {
        String taskInfo = origin.getText().replace("/task", "").trim();
        String[] parts = taskInfo.split("\\s+");
        if (parts.length >= 4) {
            String dateStr = parts[0];
            String timeStr = parts[1];
            String taskName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length - 1));
            String url = parts[parts.length - 1];
            try {
                LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                LocalTime dueTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                Long chatId = origin.getChatId();
                if (chatService.find(chatId) != null) {
                    DueTask task = new DueTask();
                    task.setDueDate(dueDate);
                    task.setDueTime(dueTime);
                    task.setTaskName(taskName);
                    task.setUrl(url);
                    task.setChat(chatService.find(chatId));
                    taskService.save(task);
                    reply(origin, "✔ ");
                } else {
                    reply(origin, "Цей чат ще не зареєстровано у системі");
                }
            } catch (DateTimeParseException e) {
                reply(origin, "Неправильний формат дати або часу. Будь ласка, використовуйте формат HH:mm для часу, та dd.MM.yyyy");
            }
        } else {
            sendMessage(origin, "Неповна команда. Будь ласка, використовуйте /task dd.MM.yyyy HH:mm [Завдання] [Посилання]");
        }
    }
}

