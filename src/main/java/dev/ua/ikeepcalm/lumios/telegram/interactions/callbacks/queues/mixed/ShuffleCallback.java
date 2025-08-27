package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.queues.mixed;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.UUID;

@Component
@BotCallback(endsWith = "mixed-shuffle")
public class ShuffleCallback extends ServicesShortcut implements Interaction {

    @Override

    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-mixed-shuffle", "");
        String callbackQueryId = message.getId();
        MixedQueue mixedQueue;
        try {
            mixedQueue = queueService.findMixedById(UUID.fromString(receivedCallback));
            for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
                if (chatMember.getUser().getId().equals(message.getFrom().getId()) || message.getFrom().getUserName().equals("ikeepcalm")) {
                    if (!mixedQueue.isShuffled()) {
                        mixedQueue.shuffleContents();

                        SimpleQueue simpleQueue = new SimpleQueue();
                        simpleQueue.setId(mixedQueue.getId());
                        simpleQueue.setMessageId(mixedQueue.getMessageId());
                        simpleQueue.setAlias(mixedQueue.getAlias());
                        simpleQueue.setChatId(mixedQueue.getChatId());
                        for (MixedUser mixedUser : mixedQueue.getContents()) {
                            SimpleUser simpleUser = new SimpleUser();
                            simpleUser.setId(mixedUser.getId());
                            simpleUser.setName(mixedUser.getName());
                            simpleUser.setUsername(mixedUser.getUsername());
                            simpleUser.setAccountId(mixedUser.getAccountId());
                            simpleUser.setSimpleQueue(simpleQueue);
                            simpleQueue.getContents().add(simpleUser);
                        }
                        queueService.deleteMixedQueue(mixedQueue);
                        simpleQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(message.getMessage().getChatId(), simpleQueue)).getMessageId());
                        queueService.save(simpleQueue);
                        this.telegramClient.sendAnswerCallbackQuery("Успішно перемішано цю чергу!", callbackQueryId);
                    } else {
                        this.telegramClient.sendAnswerCallbackQuery("Ця черга вже перемішана!", callbackQueryId);
                    }
                    break;
                }
            }
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
