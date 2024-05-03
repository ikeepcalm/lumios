package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks.mixed;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component("mixed-join")
public class JoinCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-join", "");
        String callbackQueryId = message.getId();
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
                                (QueueUpdateUtil.updateMessage(message.getMessage().getChatId(), mixedQueue))
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

