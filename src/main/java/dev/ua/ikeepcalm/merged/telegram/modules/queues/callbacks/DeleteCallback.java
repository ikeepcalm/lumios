package dev.ua.ikeepcalm.merged.telegram.modules.queues.callbacks;

import dev.ua.ikeepcalm.merged.database.entities.queue.QueueItself;
import dev.ua.ikeepcalm.merged.telegram.modules.CallbackParent;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.UUID;

@Component
public class DeleteCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-delete", "");
        String callbackQueryId = message.getId();
        instantiateUpdate(message);
        for (ChatMember chatMember : absSender.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
            if (chatMember.getUser().getId().equals(message.getFrom().getId())) {
                QueueItself queueItself = queueLifecycleUtil.getQueue(UUID.fromString(receivedCallback));
                queueLifecycleUtil.deleteQueue(queueItself);
                absSender.sendRemoveMessage(new RemoveMessage(queueItself.getMessageId(), super.message.getChatId()));
                sendMessage("@".concat(message.getFrom().getUserName()).concat(" видалив чергу: ").concat(queueItself.getAlias()).concat("!"));
                break;
            } absSender.sendAnswerCallbackQuery("Авторизувати цю дію може лише адміністратор цього чату!", callbackQueryId);
        }
    }
}

