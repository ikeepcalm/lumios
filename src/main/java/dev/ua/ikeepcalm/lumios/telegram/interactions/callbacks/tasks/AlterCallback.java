package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskScope;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.TaskState;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.UpdateConsumer;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.TaskMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@BotCallback(startsWith = "task-alter-")
public class AlterCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String[] data = callbackQuery.getData().split("-");

        String action = data[2];

        if (action.equals("delete")) {
            long taskId = Long.parseLong(data[3]);
            DueTask task;
            try {
                task = taskService.findTaskById(chat.getChatId(), taskId);
            } catch (NoSuchEntityException e) {
                telegramClient.sendAnswerCallbackQuery(translationService.getMessage("task.error.not_found", chat), callbackQuery.getId());
                return;
            }

            EditMessage editMessage = new EditMessage();
            editMessage.setChatId(callbackQuery.getMessage().getChatId());
            editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessage.setText(translationService.getMessage("task.delete.confirm", chat));
            editMessage.setReplyKeyboard(TaskMarkupUtil.createDeleteTaskKeyboard(task.getId()));

            editMessage(editMessage);
        } else {
            TaskState state = switch (action) {
                case "name" -> TaskState.WAITING_FOR_NAME;
                case "desc" -> TaskState.WAITING_FOR_DESC;
                case "date" -> TaskState.WAITING_FOR_DATE;
                case "link" -> TaskState.WAITING_FOR_URL;
                case "scope" -> TaskState.WAITING_FOR_SCOPE;
                case "attachments" -> TaskState.WAITING_FOR_ATTACHMENT;
                default -> TaskState.STAND_BY;
            };

            if (state != TaskState.STAND_BY) {
                if (state == TaskState.WAITING_FOR_SCOPE) {
                    if (data.length > 4) {
                        String scope = data[3];
                        TaskScope taskScope = TaskScope.EVERYONE;
                        switch (scope) {
                            case "onlyme" -> taskScope = TaskScope.SINGLE;
                            case "everyone" -> taskScope = TaskScope.EVERYONE;
                            case "notme" -> taskScope = TaskScope.NOT_ME;
                        }

                        long taskId = Long.parseLong(data[4]);
                        DueTask task;
                        try {
                            task = taskService.findTaskById(chat.getChatId(), taskId);
                        } catch (NoSuchEntityException e) {
                            telegramClient.sendAnswerCallbackQuery(translationService.getMessage("task.error.not_found", chat), callbackQuery.getId());
                            return;
                        }

                        task.setScope(taskScope);
                        taskService.save(task);

                        EditMessage editMessage = new EditMessage();
                        editMessage.setChatId(callbackQuery.getMessage().getChatId());
                        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                        editMessage.setText(translationService.getMessage("task.range.success", chat));
                        editMessage(editMessage);

                        TextMessage textMessage = TaskMarkupUtil.buildTaskTextMessage(task, task.getId(), chat, translationService);
                        textMessage.setChatId(callbackQuery.getMessage().getChatId());
                        sendMessage(textMessage, (Message) callbackQuery.getMessage());
                        telegramClient.sendAnswerCallbackQuery(translationService.getMessage("task.range.success", chat), callbackQuery.getId());
                    } else {
                        long taskId = Long.parseLong(data[3]);
                        EditMessage editMessage = new EditMessage();
                        editMessage.setChatId(callbackQuery.getMessage().getChatId());
                        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                        editMessage.setReplyKeyboard(TaskMarkupUtil.createScopeKeyboard(taskId));
                        editMessage.setText(translationService.getMessage("task.edit.scope.select", chat));
                        editMessage(editMessage);
                    }
                } else {
                    long taskId = Long.parseLong(data[3]);
                    DueTask task;
                    try {
                        task = taskService.findTaskById(chat.getChatId(), taskId);
                    } catch (NoSuchEntityException e) {
                        telegramClient.sendAnswerCallbackQuery(translationService.getMessage("task.error.not_found", chat), callbackQuery.getId());
                        return;
                    }

                    task.setState(state);
                    EditMessage editMessage = new EditMessage();
                    editMessage.setChatId(callbackQuery.getMessage().getChatId());
                    editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                    editMessage.setText(translationService.getMessage("task.edit.prompt", chat));
                    editMessage(editMessage);
                    UpdateConsumer.waitingTasks.put(user.getUserId(), task.getId());
                }
            }
        }
    }
}
