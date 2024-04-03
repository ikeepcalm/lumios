package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CallbackParent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component("simple-join")
public class JoinCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-join", "");
        String callbackQueryId = message.getId(); handleUpdate(message);
        SimpleQueue simpleQueue;
        try {
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setName(message.getFrom().getFirstName());
            simpleUser.setAccountId(message.getFrom().getId());
            simpleUser.setUsername(message.getFrom().getUserName());
            if (!simpleQueue.getContents().contains(simpleUser)) {
                simpleQueue.getContents().add(simpleUser);
                simpleQueue.setMessageId(telegramClient.sendEditMessage
                                (QueueUpdateUtil.updateMessage((Message) message.getMessage(), simpleQueue))
                        .getMessageId());
                queueService.save(simpleQueue);
                this.telegramClient.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", callbackQueryId);
            } else {
                this.telegramClient.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", callbackQueryId);
            }
        } catch (NoSuchEntityException e) {
            this.telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }

}

