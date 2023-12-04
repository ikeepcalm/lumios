package dev.ua.ikeepcalm.merged.telegram.modules.queues;

import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.*;

import dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.mixed.ShuffleCallback;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.commands.MixedCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.commands.QueueCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.HandlerParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class QueueHandler implements HandlerParent {

    private final QueueCommand queueCommand;
    private final MixedCommand mixedCommand;
    private final JoinCallback simpleJoinCallback;
    private final dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.mixed.JoinCallback mixedJoinCallback;
    private final ShuffleCallback shuffleCallback;
    private final FlushCallback flushCallback;
    private final ExitCallback exitCallback;
    private final DeleteCallback deleteCallback;
    private final NotifyCallback notifyCallback;

    public QueueHandler(QueueCommand queueCommand,
                        MixedCommand mixedCommand, JoinCallback simpleJoinCallback,
                        dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.mixed.JoinCallback mixedJoinCallback, ShuffleCallback shuffleCallback, FlushCallback flushCallback,
                        ExitCallback exitCallback,
                        DeleteCallback deleteCallback,
                        NotifyCallback notifyCallback) {
        this.queueCommand = queueCommand;
        this.mixedCommand = mixedCommand;
        this.simpleJoinCallback = simpleJoinCallback;
        this.mixedJoinCallback = mixedJoinCallback;
        this.shuffleCallback = shuffleCallback;
        this.flushCallback = flushCallback;
        this.exitCallback = exitCallback;
        this.deleteCallback = deleteCallback;
        this.notifyCallback = notifyCallback;
    }


    private void manageCommands(Update update) {
        Message message = update.getMessage();
        String commandText = message.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            if (command.equals("/queue")) {
                queueCommand.processUpdate(message);
            } else if (command.equals("/mixed")) {
                mixedCommand.processUpdate(message);
            }
        }
    }

    private void manageCallbacks(Update update) {
        String callback = update.getCallbackQuery().getData();
        CallbackQuery message = update.getCallbackQuery();
        if (callback.endsWith("-simple-join")) {
            simpleJoinCallback.processUpdate(message);
        } else if (callback.endsWith("-simple-flush")) {
            flushCallback.processUpdate(message);
        } else if (callback.endsWith("-simple-exit")) {
            exitCallback.processUpdate(message);
        } else if (callback.endsWith("-simple-delete")){
            deleteCallback.processUpdate(message);
        } else if (callback.endsWith("-simple-notify")){
            notifyCallback.processUpdate(message);
        } else if (callback.endsWith("-mixed-join")) {
            mixedJoinCallback.processUpdate(message);
        } else if (callback.endsWith("-mixed-shuffle")) {
            shuffleCallback.processUpdate(message);
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

