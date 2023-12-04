package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks.mixed;

import dev.ua.ikeepcalm.merged.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.merged.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueUpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.UUID;

@Component
public class ShuffleCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-mixed-shuffle", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        MixedQueue mixedQueue = mixedQueueLifecycle.getMixedQueue(UUID.fromString(receivedCallback));
        for (ChatMember chatMember : absSender.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
            if (chatMember.getUser().getId().equals(message.getFrom().getId())) {
                if (!mixedQueue.isShuffled()) {
                    mixedQueue.shuffleContents();

                    SimpleQueue simpleQueue = new SimpleQueue();
                    simpleQueue.setId(mixedQueue.getId());
                    simpleQueue.setMessageId(mixedQueue.getMessageId());
                    simpleQueue.setAlias(mixedQueue.getAlias());
                    simpleQueue.setContents(mixedQueue.getContents());

                    simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
                    mixedQueueLifecycle.deleteMixedQueue(mixedQueue);

                    simpleQueue.setMessageId(absSender.sendEditMessage
                            (QueueUpdateUtil.updateMessage(message.getMessage(), simpleQueue))
                            .getMessageId());
                    simpleQueueLifecycle.saveSimpleQueue(simpleQueue);
                    this.absSender.sendAnswerCallbackQuery("Успішно перемішано цю чергу!", callbackQueryId);
                } else {
                    this.absSender.sendAnswerCallbackQuery("Ця черга вже перемішана!", callbackQueryId);
                }
                break;
            }
        }
    }

}
