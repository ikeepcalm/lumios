/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.modules.queues;

import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.ExitCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.FlushCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.JoinCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.commands.QueueCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.commands.SaveQueuesCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.ModuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class QueueHandler implements ModuleHandler {
    private final QueueCommand queueCommand;
    private final SaveQueuesCommand saveQueuesCommand;

    private final JoinCallback joinCallback;
    private final FlushCallback flushCallback;
    private final ExitCallback exitCallback;


    @Autowired
    public QueueHandler(QueueCommand queueCommand,
                        SaveQueuesCommand saveQueuesCommand, JoinCallback joinCallback,
                        FlushCallback flushCallback,
                        ExitCallback exitCallback) {
        this.queueCommand = queueCommand;
        this.saveQueuesCommand = saveQueuesCommand;
        this.joinCallback = joinCallback;
        this.flushCallback = flushCallback;
        this.exitCallback = exitCallback;
    }

    private void manageCommands(Update update) {
        Message origin = update.getMessage();
        String commandText = origin.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            switch (command){
                case "/queue" -> queueCommand.execute(origin);
                case "/save" -> saveQueuesCommand.execute(origin);
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

