package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.UpdateConsumer;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.TaskMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@BotUpdate
public class TaskUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update) {
        long userId = update.getMessage().getFrom().getId();
        if (UpdateConsumer.waitingTasks.containsKey(userId)) {
            DueTask task;
            try {
                task = taskService.findTaskById(update.getMessage().getChatId(), UpdateConsumer.waitingTasks.get(userId));
            } catch (NoSuchEntityException e) {
                return;
            }

            switch (task.getState()) {
                case WAITING_FOR_NAME -> {
                    task.setTaskName(update.getMessage().getText());
                    task.setState(TaskState.NOT_COMPLETED);
                    UpdateConsumer.waitingTasks.remove(userId);
                }
                case WAITING_FOR_DATE -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dueDate = LocalDateTime.parse(update.getMessage().getText(), formatter);
                    task.setDueDate(dueDate.toLocalDate());
                    task.setDueTime(dueDate.toLocalTime());
                    task.setState(TaskState.NOT_COMPLETED);
                    UpdateConsumer.waitingTasks.remove(userId);
                }
                case WAITING_FOR_DESC -> {
                    task.setDescription(update.getMessage().getText());
                    task.setState(TaskState.NOT_COMPLETED);
                    UpdateConsumer.waitingTasks.remove(userId);
                }
                case WAITING_FOR_URL -> {
                    if (isValidUrl(update.getMessage().getText())) {
                        task.setUrl(update.getMessage().getText());
                        task.setState(TaskState.NOT_COMPLETED);
                        UpdateConsumer.waitingTasks.remove(userId);
                    } else {
                        sendMessage("Невірний формат посилання. Спробуйте ще раз", update.getMessage());
                    }
                }
                case WAITING_FOR_ATTACHMENT -> {
                    if (update.getMessage().hasPhoto()){
                        task.setAttachment(update.getMessage().getPhoto().getFirst().getFileId());
                    } else if (update.getMessage().hasDocument()){
                        task.setAttachment(update.getMessage().getDocument().getFileId());
                    } else {
                        sendMessage("Невірний формат файлу. Спробуйте ще раз", update.getMessage());
                        return;
                    }
                    task.setState(TaskState.NOT_COMPLETED);

                    UpdateConsumer.waitingTasks.remove(userId);
                }
            }
            taskService.save(task);
            TextMessage editMessage = TaskMarkupUtil.buildTaskTextMessage(task, task.getId());
            editMessage.setChatId(update.getMessage().getChatId());
            sendMessage(editMessage, update.getMessage());
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
