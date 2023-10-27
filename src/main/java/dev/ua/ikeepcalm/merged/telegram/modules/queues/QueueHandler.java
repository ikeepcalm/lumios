package dev.ua.ikeepcalm.merged.telegram.modules.queues;

import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.*;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.commands.QueueCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class QueueHandler implements HandlerParent {
    private final QueueCommand queueCommand;
    private final JoinCallback joinCallback;
    private final FlushCallback flushCallback;
    private final ExitCallback exitCallback;
    private final DeleteCallback deleteCallback;
    private final NotifyCallback notifyCallback;

    public QueueHandler(QueueCommand queueCommand, JoinCallback joinCallback,
                        FlushCallback flushCallback,
                        ExitCallback exitCallback, DeleteCallback deleteCallback, NotifyCallback notifyCallback) {
        this.queueCommand = queueCommand;
        this.joinCallback = joinCallback;
        this.flushCallback = flushCallback;
        this.exitCallback = exitCallback;
        this.deleteCallback = deleteCallback;
        this.notifyCallback = notifyCallback;
    }

    private void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            if (command.equals("/queue")) {
                queueCommand.execute(origin);
            }
        }
    }

    private void manageCallbacks(Update update) {
        String callback = update.getCallbackQuery().getData();
        CallbackQuery origin = update.getCallbackQuery();
        if (callback.endsWith("-join")) {
            callback = callback.replace("-join", "");
            joinCallback.manage(callback, origin);
        } else if (callback.endsWith("-flush")) {
            callback = callback.replace("-flush", "");
            flushCallback.manage(callback, origin);
        } else if (callback.endsWith("-exit")) {
            callback = callback.replace("-exit", "");
            exitCallback.manage(callback, origin);
        } else if (callback.endsWith("-delete")){
            callback = callback.replace("-delete", "");
            deleteCallback.manage(callback, origin);
        } else if (callback.endsWith("-notify")){
            callback = callback.replace("-notify", "");
            notifyCallback.manage(callback, origin);
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.hasMessage()){
            manageCommands(update);
        } else if (update.hasCallbackQuery()){
            manageCallbacks(update);
        }
    }
}

