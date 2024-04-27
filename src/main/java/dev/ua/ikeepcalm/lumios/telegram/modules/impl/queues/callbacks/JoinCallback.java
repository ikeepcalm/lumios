package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component("simple-join")
public class JoinCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-join", "");
        String callbackQueryId = message.getId();
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

