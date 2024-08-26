package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.QueueUpdateUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.UUID;

@Component
@BotCallback(endsWith = "simple-exit")
public class ExitCallback extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String receivedCallback = message.getData().replace("-simple-exit", "");
        String callbackQueryId = message.getId();
        try {
            UUID queueId = UUID.fromString(receivedCallback);
            SimpleQueue simpleQueue = queueService.findSimpleById(queueId);

            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setName(message.getFrom().getFirstName());
            simpleUser.setAccountId(message.getFrom().getId());
            simpleUser.setUsername(message.getFrom().getUserName() == null ? "ukhilyant" : message.getFrom().getUserName());

            List<SimpleUser> queueContents = simpleQueue.getContents();

            if (queueContents.isEmpty()) {
                telegramClient.sendAnswerCallbackQuery("Помилка! Черга порожня!", callbackQueryId);
                return;
            }

            SimpleUser lastUserInQueue = queueContents.getFirst();

            if (lastUserInQueue.equals(simpleUser)) {
                if (simpleQueue.flushUser(simpleUser)) {
                    queueContents.remove(simpleUser);
                    simpleQueue.setMessageId(this.telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(message.getMessage().getChatId(), simpleQueue)).getMessageId());
                    queueService.save(simpleQueue);

                    this.telegramClient.sendAnswerCallbackQuery("Йоу! Вітаю із виходом з цієї черги. Тепер можна і розслабитися...", callbackQueryId);

                    if (queueContents.isEmpty()) {
                        RemoveMessage removeMessage = new RemoveMessage(simpleQueue.getMessageId(), message.getMessage().getChatId());
                        try {
                            this.telegramClient.sendRemoveMessage(removeMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        queueService.deleteSimpleQueue(simpleQueue);
                    } else {
                        this.telegramClient.sendTextMessage(QueueMarkupUtil.createNotification(message.getMessage().getChatId(), simpleQueue));
                    }
                }
            } else if (queueContents.contains(simpleUser)) {
                queueContents.remove(simpleUser);
                simpleQueue.setMessageId(telegramClient.sendEditMessage(QueueUpdateUtil.updateMessage(message.getMessage().getChatId(), simpleQueue)).getMessageId());
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

