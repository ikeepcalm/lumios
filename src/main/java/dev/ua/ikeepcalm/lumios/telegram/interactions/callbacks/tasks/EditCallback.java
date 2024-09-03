package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.TaskMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.HashMap;
import java.util.List;

@Component
@BotCallback(startsWith = "task-edit-")
public class EditCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String data = message.getData();
        if (data.equals("task-edit-")) {
            List<DueTask> tasks = taskService.getTasksForCurrentChat(chat);
            HashMap<String, String> taskMap = new HashMap<>();
            for (DueTask task : tasks) {
                taskMap.put(String.valueOf(task.getId()), task.getTaskName());
            }

            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(message.getMessage().getChatId());
            editMessage.setMessageId(message.getMessage().getMessageId());
            editMessage.setText("Оберіть завдання для редагування:");
            editMessage.setReplyKeyboard(TaskMarkupUtil.createTasksKeyboard(taskMap));
            editMessage(editMessage);
        } else {
            String[] parts = message.getData().split("-");
            long taskId = Long.parseLong(parts[2]);
            DueTask task = null;
            try {
                task = taskService.findTaskById(chat.getChatId(), taskId);
            } catch (NoSuchEntityException e) {
                telegramClient.sendAnswerCallbackQuery("Завдання не знайдено. Схоже на серверну помилку, зверніться до підтримки!", message.getId());
                return;
            }
            task.setState(TaskState.NOT_COMPLETED);
            taskService.save(task);
            EditMessage editMessage = TaskMarkupUtil.buildTaskEditMessage(task);
            editMessage.setChatId(message.getMessage().getChatId());
            editMessage.setMessageId(message.getMessage().getMessageId());
            editMessage(editMessage);
        }
    }
}

