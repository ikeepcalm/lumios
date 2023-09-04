package dev.ua.ikeepcalm.queueupnow.telegram.executing.callbacks;

import dev.ua.ikeepcalm.queueupnow.entities.Queue;
import dev.ua.ikeepcalm.queueupnow.entities.User;
import dev.ua.ikeepcalm.queueupnow.managers.QueueManager;
import dev.ua.ikeepcalm.queueupnow.telegram.executing.Executable;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.PurgeMessage;
import dev.ua.ikeepcalm.queueupnow.utils.DublicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

@Component
public class ExitCallback extends Executable {

    @Autowired
    private QueueManager queueManager;

    public void manage(String receivedCallback, CallbackQuery origin) {
        Queue queue = queueManager.getQueue(UUID.fromString(receivedCallback));

        User user = new User();
        user.setName(origin.getFrom().getFirstName());
        user.setAccountId(origin.getFrom().getId());
        user.setUsername(origin.getFrom().getUserName());

        if (queue.getContents().peek().equals(user)) {
            if (queue.flushUser(user)) {
                queue.setMessageId(telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queue)).getMessageId());
                queueManager.updateQueue(queue);
                telegramService.sendAnswerCallbackQuery("Ха! Ти помилився кнопкою. Але нічого страшного, я сповіщу наступного за тобою!", origin.getId());
                if (queue.getContents().isEmpty()) {
                    PurgeMessage purgeMessage = new PurgeMessage((int) queue.getMessageId(), origin.getMessage().getChatId());
                    telegramService.sendPurgeMessage(purgeMessage);
                } else {
                    telegramService.sendMultiMessage(DublicateUtil.createNotification(origin.getMessage().getChatId(), queue));
                }
            }
        } else if (queue.getContents().contains(user)) {
            queue.removeUser(user);
            queue.setMessageId(telegramService.sendAlterMessage(DublicateUtil.updateMessage(origin.getMessage(), queue)).getMessageId());
            queueManager.updateQueue(queue);
            telegramService.sendAnswerCallbackQuery("Щось важливіше за це? Хмм... Твоє право", origin.getId());
            if (queue.getContents().isEmpty()) {
                PurgeMessage purgeMessage = new PurgeMessage((int) queue.getMessageId(), origin.getMessage().getChatId());
                telegramService.sendPurgeMessage(purgeMessage);
                queueManager.deleteQueue(queue);
            }
        } else {
            telegramService.sendAnswerCallbackQuery("Щоб вийти з цієї черги треба спочатку приєднатися до неї! Гадаю, що ти тицнув сюди випадково ^-^", origin.getId());

        }
    }
}
