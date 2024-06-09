package dev.ua.ikeepcalm.lumios.telegram.modules.impl.queues.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CallbackParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.UUID;

@Component
public class DeleteCallback extends CallbackParent {

    @Override
    public void processUpdate(CallbackQuery message) {
        String receivedCallback = message.getData().replace("-simple-delete", "");
        try {
            for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(message.getMessage().getChatId()))) {
                if (chatMember.getUser().getId().equals(message.getFrom().getId()) || message.getFrom().getUserName().equals("ikeepcalm")) {
                    SimpleQueue simpleQueue;
                    try {
                        simpleQueue = queueService.findSimpleById(UUID.fromString(receivedCallback));
                        queueService.deleteSimpleQueue(simpleQueue);
                        telegramClient.sendRemoveMessage(new RemoveMessage(simpleQueue.getMessageId(), super.message.getChatId()));
                        TextMessage textMessage = new TextMessage();
                        textMessage.setChatId(message.getMessage().getChatId());
                        textMessage.setText("@".concat(message.getFrom().getUserName()).concat(" видалив чергу: ").concat(simpleQueue.getAlias()).concat("!"));
                        sendMessage(textMessage);
                        break;
                    } catch (NoSuchEntityException e) {
                        sendMessage("Помилка! Не знайдено чергу з таким ID!");
                    }
                }
            }
        } catch (TelegramApiException ignored) {
        }
    }
}

