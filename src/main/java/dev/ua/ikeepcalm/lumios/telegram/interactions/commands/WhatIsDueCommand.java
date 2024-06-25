package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@BotCommand(command = "due")
public class WhatIsDueCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        List<DueTask> tasks = taskService.getTasksForCurrentChat(chat);

        LocalDate today = LocalDate.now();
        LocalDate maxDueDate = today.plusDays(14);
        List<DueTask> filteredTasks = tasks.stream()
                .filter(task -> !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(maxDueDate)
                                && !(task.getDueDate().isEqual(today) && task.getDueTime().isBefore(LocalTime.now())))
                .toList();

        if (!filteredTasks.isEmpty()) {
            Map<LocalDate, List<DueTask>> groupedTasks = filteredTasks.stream()
                    .collect(Collectors.groupingBy(DueTask::getDueDate));

            Map<LocalDate, List<DueTask>> sortedTasks = groupedTasks.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


            DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.of("uk"));
            StringBuilder messageBuilder = new StringBuilder();

            for (LocalDate dueDate : sortedTasks.keySet()) {
                List<DueTask> tasksForDate = groupedTasks.get(dueDate);
                LocalDate tomorrow = today.plusDays(1);

                if (dueDate.isEqual(today)) {
                    messageBuilder.append("\n*СЬОГОДНІ*\n");
                } else if (dueDate.isEqual(tomorrow)) {
                    messageBuilder.append("\n*ЗАВТРА*\n");
                } else {
                    messageBuilder.append("\n*").append(dayOfWeekFormatter.format(dueDate.getDayOfWeek()).toUpperCase()).append("*\n");
                }

                for (DueTask task : tasksForDate) {
                    messageBuilder.append("(ID:").append(task.getId()).append(") > ").append(task.getDueTime()).append(" - ").append("[").append(task.getTaskName()).append("](").append(task.getUrl()).append(")\n");
                }
            }

            sendMessage(messageBuilder.toString(), ParseMode.MARKDOWN, update.getMessage());
        } else {
            sendMessage("Нічого немає, можна відпочивати!", update.getMessage());
        }
    }
}
