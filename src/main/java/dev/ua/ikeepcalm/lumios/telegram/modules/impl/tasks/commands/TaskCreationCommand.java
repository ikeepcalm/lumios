package dev.ua.ikeepcalm.lumios.telegram.modules.impl.tasks.commands;

import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Component
public class TaskCreationCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
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

    private boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
