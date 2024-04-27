package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues;

import dev.ua.ikeepcalm.lumios.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.DeleteCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.ExitCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.JoinCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.NotifyCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.mixed.ShuffleCallback;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.commands.MixedCommand;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.commands.QueueCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class QueueHandler implements HandlerParent {

    private final QueueCommand queueCommand;
    private final MixedCommand mixedCommand;
    private final JoinCallback simpleJoinCallback;
    private final dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.mixed.JoinCallback mixedJoinCallback;
    private final ShuffleCallback shuffleCallback;
    private final ExitCallback exitCallback;
    private final DeleteCallback deleteCallback;
    private final NotifyCallback notifyCallback;

    public QueueHandler(QueueCommand queueCommand,
                        MixedCommand mixedCommand, JoinCallback simpleJoinCallback,
                        dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.mixed.JoinCallback mixedJoinCallback, ShuffleCallback shuffleCallback,
                        ExitCallback exitCallback,
                        DeleteCallback deleteCallback,
                        NotifyCallback notifyCallback) {
        this.queueCommand = queueCommand;
        this.mixedCommand = mixedCommand;
        this.simpleJoinCallback = simpleJoinCallback;
        this.mixedJoinCallback = mixedJoinCallback;
        this.shuffleCallback = shuffleCallback;
        this.exitCallback = exitCallback;
        this.deleteCallback = deleteCallback;
        this.notifyCallback = notifyCallback;
    }


    private void manageCommands(Update update) {
        Message message = update.getMessage();
        if (message.getChat().getType().equals("private")) {
            return;
        }

        String commandText = message.getText();
        if (commandText != null && commandText.startsWith("/")) {
            String[] parts = commandText.split("\\s+", 10);
            String command = parts[0];
            command = command.replace("@queueupnow_bot", "");
            if (command.equals("/queue")) {
                queueCommand.handleUpdate(message);
            } else if (command.equals("/mixed")) {
                mixedCommand.handleUpdate(message);
            }
        }
    }

    private void manageCallbacks(Update update) {
        String callback = update.getCallbackQuery().getData();
        CallbackQuery message = update.getCallbackQuery();
        if (callback.endsWith("-simple-join")) {
            simpleJoinCallback.handleUpdate(message);
        } else if (callback.endsWith("-simple-exit")) {
            exitCallback.handleUpdate(message);
        } else if (callback.endsWith("-simple-delete")) {
            deleteCallback.handleUpdate(message);
        } else if (callback.endsWith("-simple-notify")) {
            notifyCallback.handleUpdate(message);
        } else if (callback.endsWith("-mixed-join")) {
            mixedJoinCallback.handleUpdate(message);
        } else if (callback.endsWith("-mixed-shuffle")) {
            shuffleCallback.handleUpdate(message);
        }
    }

    @Override
    public void dispatchUpdate(Update update) {
        if (update.hasMessage()) {
            manageCommands(update);
        } else if (update.hasCallbackQuery()) {
            manageCallbacks(update);
        }
    }
}

