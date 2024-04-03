package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.tasks.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Component
public class TaskCreationCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        String taskInfo = message.getText().replace("/task", "").trim();
        String[] parts = taskInfo.split("\\s+");
        if (parts.length >= 4) {
            String dateStr = parts[0];
            String timeStr = parts[1];
            String taskName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length - 1));
            String url = parts[parts.length - 1];
            try {
                LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                LocalTime dueTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                DueTask task = new DueTask();
                task.setDueDate(dueDate);
                task.setDueTime(dueTime);
                task.setTaskName(taskName);
                task.setUrl(url);
                task.setChat(reverenceChat);
                taskService.save(task);
                sendConfirmationReaction(message);
            } catch (DateTimeParseException e) {
                sendMessage("Неправильний формат дати або часу. Будь ласка, використовуйте формат HH:mm для часу, та dd.MM.yyyy");
            }
        } else {
            sendMessage("Неповна команда. Будь ласка, використовуйте /task dd.MM.yyyy HH:mm [Завдання] [Посилання]");
        }
    }
}



