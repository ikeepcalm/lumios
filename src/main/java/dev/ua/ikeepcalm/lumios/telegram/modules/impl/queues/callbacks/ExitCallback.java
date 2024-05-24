package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.UUID;

@Component
public class ExitCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-exit", "");
        String callbackQueryId = message.getId();
        try {
            UUID queueId = UUID.fromString(receivedCallback);
            SimpleQueue simpleQueue = queueService.findSimpleById(queueId);

            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setName(message.getFrom().getFirstName());
            simpleUser.setAccountId(message.getFrom().getId());
            simpleUser.setUsername(message.getFrom().getUserName());

            List<SimpleUser> queueContents = simpleQueue.getContents();

            if (queueContents.isEmpty()) {
                telegramClient.sendAnswerCallbackQuery("Помилка! Черга порожня!", callbackQueryId);
                return;
            }

            SimpleUser lastUserInQueue = queueContents.getFirst();

            if (lastUserInQueue.equals(simpleUser)) {
                if (simpleQueue.flushUser(simpleUser)) {
                    queueContents.remove(simpleUser);
                    simpleQueue.setMessageId(this.telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(super.message.getChatId(), simpleQueue)).getMessageId());
                    queueService.save(simpleQueue);

                    this.telegramClient.sendAnswerCallbackQuery("Йоу! Вітаю із виходом з цієї черги. Тепер можна і розслабитися...", callbackQueryId);

                    if (queueContents.isEmpty()) {
                        RemoveMessage removeMessage = new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId());
                        try {
                            this.telegramClient.sendRemoveMessage(removeMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        queueService.deleteSimpleQueue(simpleQueue);
                    } else {
                        this.telegramClient.sendTextMessage(QueueMarkupUtil.createNotification(super.message.getChatId(), simpleQueue));
                    }
                }
            } else if (queueContents.contains(simpleUser)) {
                queueContents.remove(simpleUser);
                simpleQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(super.message.getChatId(), simpleQueue)).getMessageId());
                queueService.save(simpleQueue);
                telegramClient.sendAnswerCallbackQuery("Хочеш вийти? Ну добре, виходь, ніхто ж тебе тут насильно не тримає...", callbackQueryId);
            }
        } catch (IllegalArgumentException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Невірний формат ID черги!", callbackQueryId);
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чергу з таким ID!", callbackQueryId);
        }
    }

}

