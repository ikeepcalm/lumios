package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@BotCommand(command = "revive")
public class ReviveCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        try {
            for (ChatMember chatMember : telegramClient.getChatAdministrators(String.valueOf(update.getMessage().getChatId()))) {
                if (chatMember.getUser().getId().equals(update.getMessage().getFrom().getId()) || update.getMessage().getFrom().getUserName().equals("ikeepcalm")) {
                    List<SimpleQueue> simpleQueues = queueService.findAllSimpleByChatId(chat.getChatId());
                    List<MixedQueue> mixedQueues = queueService.findAllMixedByChatId(chat.getChatId());

                    for (SimpleQueue simpleQueue : simpleQueues) {
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("Черга знайдена. Відновлюю " + simpleQueue.getAlias() + "!");
                        textMessage.setChatId(update.getMessage().getChatId());
                        textMessage.setMessageId(simpleQueue.getMessageId());
                        Message sent = telegramClient.sendTextMessage(textMessage);
                        TextMessage queueMessage = new TextMessage();
                        queueMessage.setChatId(chat.getChatId());
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(">>> ").append(simpleQueue.getAlias()).append(" <<<\n\n");
                        int id = 1;
                        for (SimpleUser iteSimpleUser : simpleQueue.getContents()) {
                            stringBuilder.append("ID: ")
                                    .append(id).append(" - ")
                                    .append(iteSimpleUser.getName())
                                    .append(" (@").append(iteSimpleUser.getUsername()).append(")\n");
                            ++id;
                        }
                        queueMessage.setText(stringBuilder.toString());
                        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(simpleQueue));
                        Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
                        telegramClient.pinChatMessage(update.getMessage().getChatId(), sendTextMessage.getMessageId());
                        simpleQueue.setMessageId(sendTextMessage.getMessageId());
                        simpleQueue.setChatId(sendTextMessage.getChatId());
                        queueService.save(simpleQueue);
                    }

                    for (MixedQueue mixedQueue : mixedQueues) {
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("Черга знайдена. Намагаюся відновити " + mixedQueue.getAlias() + "!");
                        textMessage.setChatId(update.getMessage().getChatId());
                        textMessage.setMessageId(mixedQueue.getMessageId());
                        Message sent = telegramClient.sendTextMessage(textMessage);
                        TextMessage queueMessage = new TextMessage();
                        queueMessage.setChatId(chat.getChatId());
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(">>> ").append(mixedQueue.getAlias()).append(" <<<\n\n");
                        int id = 1;
                        for (MixedUser iteSimpleUser : mixedQueue.getContents()) {
                            stringBuilder.append("ID: ")
                                    .append(id).append(" - ")
                                    .append(iteSimpleUser.getName())
                                    .append(" (@").append(iteSimpleUser.getUsername()).append(")\n");
                            ++id;
                        }
                        queueMessage.setText(stringBuilder.toString());
                        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(mixedQueue));
                        Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
                        telegramClient.pinChatMessage(update.getMessage().getChatId(), sendTextMessage.getMessageId());
                        mixedQueue.setMessageId(sendTextMessage.getMessageId());
                        mixedQueue.setChatId(chat.getChatId());
                        queueService.save(mixedQueue);
                    }
                }
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
