package dev.ua.ikeepcalm.merged.telegram.executing.commands.tasks;

import dev.ua.ikeepcalm.merged.entities.tasks.DueTask;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class ShowCommand extends Executable {
    public void execute(Message origin) {
        Long chatId = origin.getChatId();
        List<DueTask> tasks = taskService.getTasksForCurrentChat(chatService.find(chatId));

        if (!tasks.isEmpty()) {
            LocalDate today = LocalDate.now();
            LocalDate maxDueDate = today.plusDays(14);
            LocalDate tomorrow = today.plusDays(1);
            LocalTime currentTime = LocalTime.now();

            List<DueTask> filteredTasks = tasks.stream()
                    .filter(task -> !task.getDueDate().isBefore(today)
                            && !task.getDueDate().isAfter(maxDueDate)
                            && !(task.getDueDate().isEqual(today) && task.getDueTime().isBefore(currentTime)))
                    .collect(Collectors.toList());

            Collections.sort(filteredTasks, Comparator.comparing(DueTask::getDueDate));
            DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("uk"));
            StringBuilder messageBuilder = new StringBuilder();

            for (DueTask task : filteredTasks) {
                LocalDate dueDate = task.getDueDate();
                if (dueDate.isEqual(today)) {
                    messageBuilder.append("\n*СЬОГОДНІ*\n");
                } else if (dueDate.isEqual(tomorrow)) {
                    messageBuilder.append("\n*ЗАВТРА*\n");
                } else {
                    messageBuilder.append("\n*").append(dayOfWeekFormatter.format(dueDate.getDayOfWeek()).toUpperCase()).append("*\n");
                }

                messageBuilder.append("▻|").append(task.getDueTime()).append(" - ").append(task.getTaskName()).append("\n");
            }

            sendMessage(origin, messageBuilder.toString());
        } else {
            sendMessage(origin, "Нічого немає, можна відпочивати!");
        }
    }
}

