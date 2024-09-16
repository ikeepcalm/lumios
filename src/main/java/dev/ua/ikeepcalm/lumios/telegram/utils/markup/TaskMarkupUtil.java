package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

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

    public static EditMessage buildTaskEditMessage(DueTask dueTask) {
        return buildTaskEditMessage(dueTask, 0);
    }

    public static TextMessage buildTaskTextMessage(DueTask dueTask) {
        return buildTaskTextMessage(dueTask, 0);
    }

    public static TextMessage buildTaskTextMessage(DueTask dueTask, long id) {
        TextMessage textMessage = new TextMessage();
        textMessage.setText(getFormattedMessage(dueTask));
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setReplyKeyboard(TaskMarkupUtil.getEditingMarkup(String.valueOf(id != 0 ? id : dueTask.getId())));
        return textMessage;
    }

    public static EditMessage buildTaskEditMessage(DueTask dueTask, long id) {
        EditMessage editMessage = new EditMessage();
        editMessage.setText(getFormattedMessage(dueTask));
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage.setReplyKeyboard(TaskMarkupUtil.getEditingMarkup(String.valueOf(id != 0 ? id : dueTask.getId())));
        return editMessage;
    }

    private static String getFormattedMessage(DueTask dueTask) {
        return """
                *Редагування завдання*
                
                ```String НАЗВА ЗАВДАННЯ```
                > %s
                
                ```Date ДЕДЛАЙН ЗАВДАННЯ```
                > %s %S
                
                ```Scope ДІАПАЗОН ЗАВДАННЯ```
                > %s
                
                ```Attachment ДОДАТКИ ЗАВДАННЯ```
                > %s
                
                ```Description ОПИС ЗАВДАННЯ```
                > %s
                
                ```URL ГІПЕР-ПОСИЛАННЯ```
                > %s
                
                ```Author АВТОР ЗАВДАННЯ```
                > [ця людинка](tg://user?id=%d)
                
                """.formatted(
                dueTask.getTaskName() != null ? dueTask.getTaskName() : "Не встановлено",
                dueTask.getDueDate() != null ? dueTask.getDueDate() : "Не встановлено",
                dueTask.getDueTime() != null ? dueTask.getDueTime() : "Не встановлено",
                dueTask.getScope() != null ? dueTask.getScope() : "Не встановлено",
                dueTask.getAttachment() != null ? dueTask.getAttachment() : "Не встановлено",
                dueTask.getDescription() != null ? dueTask.getDescription() : "Не встановлено",
                dueTask.getUrl() != null ? dueTask.getUrl() : "Не встановлено",
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
