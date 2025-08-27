package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@BotCommand(command = "repin")
public class RepinCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        try {
            for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(update.getMessage().getChatId()))) {
                if (chatMember.getUser().getId().equals(update.getMessage().getFrom().getId()) || update.getMessage().getFrom().getUserName().equals("ikeepcalm")) {
                    List<SimpleQueue> simpleQueues = queueService.findAllSimpleByChatId(chat.getChatId());
                    List<MixedQueue> mixedQueues = queueService.findAllMixedByChatId(chat.getChatId());

                    for (SimpleQueue simpleQueue : simpleQueues) {
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("Черга знайдена. Намагаюся прикріпити " + simpleQueue.getAlias() + "!");
                        textMessage.setChatId(update.getMessage().getChatId());
                        textMessage.setMessageId(simpleQueue.getMessageId());
                        Message sent = telegramClient.sendTextMessage(textMessage);
                        telegramClient.pinChatMessage(update.getMessage().getChatId(), simpleQueue.getMessageId());
                    }

                    for (MixedQueue mixedQueue : mixedQueues) {
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("Черга знайдена. Намагаюся прикріпити " + mixedQueue.getAlias() + "!");
                        textMessage.setChatId(update.getMessage().getChatId());
                        textMessage.setMessageId(mixedQueue.getMessageId());
                        Message sent = telegramClient.sendTextMessage(textMessage);
                        telegramClient.pinChatMessage(update.getMessage().getChatId(), mixedQueue.getMessageId());
                    }
                }
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
