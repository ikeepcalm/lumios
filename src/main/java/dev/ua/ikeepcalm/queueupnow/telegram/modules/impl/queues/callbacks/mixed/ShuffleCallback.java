package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.callbacks.mixed;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CallbackParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.UUID;

@Component
public class ShuffleCallback extends CallbackParent {

    @Override
    @Transactional
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-shuffle", "");
        String callbackQueryId = message.getId();
        MixedQueue mixedQueue = null;
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
                        for (int i = 0; i < mixedQueue.getContents().size(); i++) {
                            SimpleUser simpleUser = new SimpleUser();
                            simpleUser.setName(mixedQueue.getContents().get(i).getName());
                            simpleUser.setAccountId(mixedQueue.getContents().get(i).getAccountId());
                            simpleUser.setUsername(mixedQueue.getContents().get(i).getUsername());
                            simpleQueue.getContents().add(simpleUser);
                        }

                        queueService.save(simpleQueue);
                        queueService.deleteMixedQueue(mixedQueue);

                        simpleQueue.setMessageId(telegramClient.sendEditMessage
                                        (QueueUpdateUtil.updateMessage((Message) message.getMessage(), simpleQueue))
                                .getMessageId());

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
        }
    }
}
