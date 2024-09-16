package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component("task-delete")
@BotCallback(startsWith = "task-delete-")
public class DeleteCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] data = callbackQuery.getData().split("-");
        long taskId = Long.parseLong(data[2]);
        DueTask task;
        try {
            task = taskService.findTaskById(chat.getChatId(), taskId);
        } catch (NoSuchEntityException e) {
            sendMessage("Завдання не знайдено. Схоже на серверну помилку, зверніться до підтримки!", (Message) callbackQuery.getMessage());
            return;
        }

        if (task.getState() != TaskState.NOT_COMPLETED && task.getDueDate() != null && task.getAuthor() != user.getUserId()) {
            telegramClient.sendAnswerCallbackQuery("Ви не можете видалити це завдання, оскільки ви не його автор!", callbackQuery.getId());
            return;
        }

        taskService.delete(task);

        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(callbackQuery.getMessage().getChatId());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText("Завдання успішно видалене!");
        editMessage(editMessage);
    }
}
