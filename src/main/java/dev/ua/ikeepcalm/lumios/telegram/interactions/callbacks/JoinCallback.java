package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component("simple-join")
@BotCallback(endsWith = "simple-join")
public class JoinCallback extends ServicesShortcut implements Interaction {

    @Override
    @Transactional
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-simple-join", "");
        String callbackQueryId = message.getId();
        SimpleQueue simpleQueue;
        try {
            simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setName(message.getFrom().getFirstName());
            simpleUser.setAccountId(message.getFrom().getId());
            if (message.getFrom().getUserName() == null) {
                simpleUser.setUsername("ukhilyant");
            } else {
                simpleUser.setUsername(message.getFrom().getUserName());
            }
            if (!simpleQueue.getContents().contains(simpleUser)) {
                simpleQueue.getContents().add(simpleUser);
                simpleQueue.setMessageId(telegramClient.sendEditMessage
                                (QueueUpdateUtil.updateMessage(message.getMessage().getChatId(), simpleQueue))
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

