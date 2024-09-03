package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.TaskMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@BotCallback(callback = "task-new")
public class NewCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        DueTask task = new DueTask();
        task.setAuthor(user.getUserId());
        task.setState(TaskState.NOT_COMPLETED);
        task.setChat(chat);
        long id = taskService.save(task);

        EditMessage editMessage = TaskMarkupUtil.buildTaskEditMessage(task, id);
        editMessage.setChatId(message.getMessage().getChatId());
        editMessage.setMessageId(message.getMessage().getMessageId());
        editMessage(editMessage);
    }
}

