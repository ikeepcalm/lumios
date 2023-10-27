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
import java.util.InputMismatchException;

@Component
public class TaskEditingCommand extends CommandParent {
    public void execute(Message origin) {
        String taskInfo = origin.getText().replace("/edit", "").trim();
        String[] parts = taskInfo.split("\\s+");
        if (parts.length >= 5) {
            String taskIdentifier = parts[0];
            String dateStr = parts[1];
            String timeStr = parts[2];
            String taskName = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length - 1));
            String url = parts[parts.length - 1];
            try {
                LocalDate dueDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                LocalTime dueTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                DueTask existingTask = taskService.findTaskById(Long.valueOf(taskIdentifier));
                    existingTask.setDueDate(dueDate);
                    existingTask.setDueTime(dueTime);
                    existingTask.setTaskName(taskName);
                    existingTask.setUrl(url);
                    taskService.save(existingTask);
                    reply(origin, "✔ ");
            } catch (DateTimeParseException e) {
                reply(origin, "Неправильний формат дати або часу. Будь ласка, використовуйте формат HH:mm для часу, та dd.MM.yyyy");
            } catch (InputMismatchException e){
                reply(origin, "Не вдалося знайти завдання із наданим айді!");
            }
        } else {
            sendMessage(origin, "Неповна команда. Будь ласка, використовуйте /edit [ID] dd.MM.yyyy  HH:mm [Завдання] [Посилання]");
        }
    }
}

