package dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.callbacks.mixed;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.queues.utils.QueueUpdateUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component("mixed-join")
public class JoinCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-join", "");
        String callbackQueryId = message.getId(); instantiateUpdate(message);
        MixedQueue mixedQueue;
        try {
            mixedQueue = queueService.findMixedById(UUID.fromString(receivedCallback));
            MixedUser mixedUser = new MixedUser();
            mixedUser.setName(message.getFrom().getFirstName());
            mixedUser.setAccountId(message.getFrom().getId());
            mixedUser.setUsername(message.getFrom().getUserName());
            if (!mixedQueue.getContents().contains(mixedUser)) {

                mixedQueue.getContents().add(mixedUser);
                mixedQueue.setMessageId(absSender.sendEditMessage
                                (QueueUpdateUtil.updateMessage(message.getMessage(), mixedQueue))
                        .getMessageId());
                queueService.save(mixedQueue);
                this.absSender.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", callbackQueryId);
            } else {
                this.absSender.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", callbackQueryId);
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Вибачте, але ця черга була видалена або не існує");
        }
    }
}

