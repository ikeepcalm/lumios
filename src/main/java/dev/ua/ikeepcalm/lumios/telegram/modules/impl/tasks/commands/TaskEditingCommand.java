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
import java.util.InputMismatchException;

@Component
public class TaskEditingCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        String text = message.getText().replace("@lumios_bot", "");
        String taskInfo = text.replace("/edit", "").trim();
        String[] parts = taskInfo.split("\\s+");

        if (parts.length >= 4) {
            String taskIdentifier = parts[0];
            String dateStr = parts[1];
            String timeStr = parts[2];
            String taskName;
            String url = null;

            if (isValidURL(parts[parts.length - 1])) {
                taskName = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length - 1));
                url = parts[parts.length - 1];
            } else {
                taskName = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
            }

            try {
                LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                LocalTime dueTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));

                DueTask existingTask = taskService.findTaskById(reverenceChat.getChatId(), Long.valueOf(taskIdentifier));
                existingTask.setDueDate(dueDate);
                existingTask.setDueTime(dueTime);
                existingTask.setTaskName(taskName);
                existingTask.setUrl(url);
                taskService.save(existingTask);
                sendConfirmationReaction(message);
            } catch (DateTimeParseException e) {
                sendMessage("Неправильний формат дати або часу. Будь ласка, використовуйте формат HH:mm для часу, та dd.MM.yyyy");
            } catch (InputMismatchException e) {
                sendMessage("Не вдалося знайти завдання із наданим айді!");
            }
        } else {
            sendMessage("Неповна команда. Будь ласка, використовуйте /edit [ID] dd.MM.yyyy HH:mm [Завдання] [Посилання]");
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
