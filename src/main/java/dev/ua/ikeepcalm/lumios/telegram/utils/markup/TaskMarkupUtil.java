package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskMarkupUtil {

    public static InlineKeyboardMarkup getMenuMarkup() {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton newTask = new InlineKeyboardButton("New Task \uD83D\uDD25");
        newTask.setCallbackData("task-new");

        InlineKeyboardButton editTask = new InlineKeyboardButton("Edit Task \uD83D\uDD00");
        editTask.setCallbackData("task-edit-");

        firstRow.add(newTask);
        firstRow.add(editTask);

        InlineKeyboardButton webTask = new InlineKeyboardButton("Open web-editor \uD83D\uDDD1");
        webTask.setUrl("https://www.lumios.dev/tasks");

        secondRow.add(webTask);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static EditMessage buildTaskEditMessage(DueTask dueTask, LumiosChat chat, dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService translationService) {
        return buildTaskEditMessage(dueTask, 0, chat, translationService);
    }

    public static TextMessage buildTaskTextMessage(DueTask dueTask, LumiosChat chat, dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService translationService) {
        return buildTaskTextMessage(dueTask, 0, chat, translationService);
    }

    public static TextMessage buildTaskTextMessage(DueTask dueTask, long id, LumiosChat chat, dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService translationService) {
        TextMessage textMessage = new TextMessage();
        textMessage.setText(getFormattedMessage(dueTask, chat, translationService));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setReplyKeyboard(TaskMarkupUtil.getEditingMarkup(String.valueOf(id != 0 ? id : dueTask.getId())));
        return textMessage;
    }

    public static EditMessage buildTaskEditMessage(DueTask dueTask, long id, LumiosChat chat, dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService translationService) {
        EditMessage editMessage = new EditMessage();
        editMessage.setText(getFormattedMessage(dueTask, chat, translationService));
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage.setReplyKeyboard(TaskMarkupUtil.getEditingMarkup(String.valueOf(id != 0 ? id : dueTask.getId())));
        return editMessage;
    }

    private static String getFormattedMessage(DueTask dueTask, LumiosChat chat, dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService translationService) {
        String notSet = translationService.getMessage("task.not_set", chat);
        return ("*" + translationService.getMessage("task.edit.title", chat) + "*\n\n" +
                "```String " + translationService.getMessage("task.field.name", chat) + "```\n" +
                "> %s\n\n" +
                "```Date " + translationService.getMessage("task.field.deadline", chat) + "```\n" +
                "> %s %S\n\n" +
                "```Scope " + translationService.getMessage("task.field.scope", chat) + "```\n" +
                "> %s\n\n" +
                "```Attachment " + translationService.getMessage("task.field.attachments", chat) + "```\n" +
                "> %s\n\n" +
                "```Description " + translationService.getMessage("task.field.description", chat) + "```\n" +
                "> %s\n\n" +
                "```URL " + translationService.getMessage("task.field.url", chat) + "```\n" +
                "> %s\n\n" +
                "```Author " + translationService.getMessage("task.field.author", chat) + "```\n" +
                "> [" + translationService.getMessage("task.author.mention", chat) + "](tg://user?id=%d)\n\n").formatted(
                dueTask.getTaskName() != null ? dueTask.getTaskName() : notSet,
                dueTask.getDueDate() != null ? dueTask.getDueDate() : notSet,
                dueTask.getDueTime() != null ? dueTask.getDueTime() : notSet,
                dueTask.getScope() != null ? translationService.getMessage("task.scope." + dueTask.getScope().name().toLowerCase(), chat) : notSet,
                dueTask.getAttachment() != null ? dueTask.getAttachment() : notSet,
                dueTask.getDescription() != null ? dueTask.getDescription() : notSet,
                dueTask.getUrl() != null ? dueTask.getUrl() : notSet,
                dueTask.getAuthor()
        );
    }

    public static InlineKeyboardMarkup getEditingMarkup(String id) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();
        InlineKeyboardRow thirdRow = new InlineKeyboardRow();
        InlineKeyboardRow fourthRow = new InlineKeyboardRow();
        InlineKeyboardRow fifthRow = new InlineKeyboardRow();

        InlineKeyboardButton name = new InlineKeyboardButton("Alter Name \uD83D\uDCC7");
        name.setCallbackData("task-alter-name-" + id);

        InlineKeyboardButton date = new InlineKeyboardButton("Alter Date \uD83D\uDCC5");
        date.setCallbackData("task-alter-date-" + id);

        firstRow.add(name);
        firstRow.add(date);

        InlineKeyboardButton scope = new InlineKeyboardButton("Alter Scope \uD83D\uDD2D");
        scope.setCallbackData("task-alter-scope-" + id);

        InlineKeyboardButton desc = new InlineKeyboardButton("Alter Description \uD83D\uDCDD");
        desc.setCallbackData("task-alter-desc-" + id);

        secondRow.add(scope);
        secondRow.add(desc);

        InlineKeyboardButton attachments = new InlineKeyboardButton("Add Attachments \uD83D\uDCC2");
        attachments.setCallbackData("task-alter-attachments-" + id);

        InlineKeyboardButton link = new InlineKeyboardButton("Add Hyper-Link \uD83D\uDD17");
        link.setCallbackData("task-alter-link-" + id);

        thirdRow.add(attachments);
        thirdRow.add(link);

        InlineKeyboardButton delete = new InlineKeyboardButton("Delete \uD83D\uDDD1");
        delete.setCallbackData("task-alter-delete-" + id);
        fourthRow.add(delete);

        InlineKeyboardButton finish = new InlineKeyboardButton("Finish ✅");
        finish.setCallbackData("task-finish-" + id);

        fifthRow.add(finish);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        keyboard.add(fourthRow);
        keyboard.add(fifthRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createTasksKeyboard(Map<String, String> groups) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int maxButtons = Math.min(groups.size(), 9);

        int count = 0;
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            if (count >= maxButtons) break;
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue() != null ? entry.getValue() : "[Not Set]");
            button.setCallbackData("task-edit-" + entry.getKey());
            buttons.add(button);
            count++;
        }

        int rows, columns;
        if (buttons.size() <= 6) {
            rows = 3;
            columns = 2;
        } else {
            rows = 3;
            columns = 3;
        }

        for (int i = 0; i < rows; i++) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            for (int j = 0; j < columns; j++) {
                int index = i * columns + j;
                if (index < buttons.size()) {
                    row.add(buttons.get(index));
                }
            }
            keyboard.add(row);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createScopeKeyboard(long taskId) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton onlyMe = new InlineKeyboardButton("Only Me \uD83D\uDC64");
        onlyMe.setCallbackData("task-alter-scope-onlyme-" + taskId);

        InlineKeyboardButton allInChat = new InlineKeyboardButton("All in Chat \uD83D\uDC65");
        allInChat.setCallbackData("task-alter-scope-everyone-" + taskId);

        firstRow.add(onlyMe);
        firstRow.add(allInChat);

        InlineKeyboardButton allExceptMe = new InlineKeyboardButton("All Except Me \uD83D\uDC66");
        allExceptMe.setCallbackData("task-alter-scope-exceptme-" + taskId);

        secondRow.add(allExceptMe);

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static ReplyKeyboard createDeleteTaskKeyboard(Long id) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        InlineKeyboardRow secondRow = new InlineKeyboardRow();

        InlineKeyboardButton delete = new InlineKeyboardButton("Delete \uD83D\uDDD1");
        delete.setCallbackData("task-delete-" + id);
        row.add(delete);

        InlineKeyboardButton cancel = new InlineKeyboardButton("Cancel \uD83D\uDEAB");
        cancel.setCallbackData("task-edit-" + id);
        secondRow.add(cancel);
        keyboard.add(row);
        keyboard.add(secondRow);

        return new InlineKeyboardMarkup(keyboard);
    }
}
