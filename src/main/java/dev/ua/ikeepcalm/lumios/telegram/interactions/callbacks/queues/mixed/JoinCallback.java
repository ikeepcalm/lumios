package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.queues.mixed;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

@Component("mixed-join")
@BotCallback(endsWith = "mixed-join")
public class JoinCallback extends ServicesShortcut implements Interaction {

    @Override

    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-mixed-join", "");
        String callbackQueryId = message.getId();
        MixedQueue mixedQueue;
        try {
            mixedQueue = queueService.findMixedById(UUID.fromString(receivedCallback));
            MixedUser mixedUser = new MixedUser();
            mixedUser.setName(message.getFrom().getFirstName());
            mixedUser.setAccountId(message.getFrom().getId());
            if (message.getFrom().getUserName() == null) {
                mixedUser.setUsername("ukhilyant");
            } else {
                mixedUser.setUsername(message.getFrom().getUserName());
            }
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
            sendMessage("Вибачте, але ця черга була видалена або не існує", (Message) message.getMessage());
        }
    }
}

