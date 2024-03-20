package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.callbacks.mixed;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CallbackParent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component("mixed-join")
public class JoinCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-join", "");
        String callbackQueryId = message.getId(); handleUpdate(message);
        MixedQueue mixedQueue;
        try {
            mixedQueue = queueService.findMixedById(UUID.fromString(receivedCallback));
            MixedUser mixedUser = new MixedUser();
            mixedUser.setName(message.getFrom().getFirstName());
            mixedUser.setAccountId(message.getFrom().getId());
            mixedUser.setUsername(message.getFrom().getUserName());
            if (!mixedQueue.getContents().contains(mixedUser)) {

                mixedQueue.getContents().add(mixedUser);
                mixedQueue.setMessageId(telegramClient.sendEditMessage
                                (QueueUpdateUtil.updateMessage((Message) message.getMessage(), mixedQueue))
                        .getMessageId());
                queueService.save(mixedQueue);
                this.telegramClient.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", callbackQueryId);
            } else {
                this.telegramClient.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", callbackQueryId);
            }
        } catch (NoSuchEntityException e) {
            sendMessage("Вибачте, але ця черга була видалена або не існує");
        }
    }
}

