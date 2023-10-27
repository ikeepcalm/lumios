package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueMarkupUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class NotifyCallback extends CommandParent {
    private final QueueLifecycleUtil queueLifecycleUtil;

    public NotifyCallback(QueueLifecycleUtil queueLifecycleUtil) {
        this.queueLifecycleUtil = queueLifecycleUtil;
    }

    public void manage(String receivedCallback, CallbackQuery origin) {
        QueueItself queueItself = this.queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
        absSender.sendTextMessage(QueueMarkupUtil.createNotification(origin.getMessage().getChatId(), queueItself));
        absSender.sendAnswerCallbackQuery("Сповіщення успішно надіслане!", origin.getId());
    }
}

