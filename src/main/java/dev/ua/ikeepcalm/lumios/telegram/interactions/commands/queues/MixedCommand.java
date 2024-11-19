package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.QueueMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@BotCommand(command = "mixed")
public class MixedCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        MixedQueue mixedQueue;
        Message message = update.getMessage();
        if (!message.getText().equals("/mixed") && !message.getText().equals("/mixed@lumios_bot")) {
            String alias = message.getText()
                    .replace("/mixed@lumios_bot ", "")
                    .replace("/mixed ", "")
                    .toUpperCase();
            if (alias.isBlank() || alias.length() > 20) {
                sendMessage("Назва черги повинна бути від 1 до 20 символів!", message);
                return;
            } else {
                mixedQueue = new MixedQueue(alias);
            }
        } else {
            sendMessage("Введіть назву черги після команди! Черги без назви були поміченими застарілими починаючи із версії 2.0.0!", message);
            return;
        }
        MixedUser mixedUser = new MixedUser();
        mixedUser.setName(message.getFrom().getFirstName());
        mixedUser.setAccountId(message.getFrom().getId());
        if (message.getFrom().getUserName() == null) {
            mixedUser.setUsername("ukhilyant");
        } else {
            mixedUser.setUsername(message.getFrom().getUserName());
        }
        mixedQueue.getContents().add(mixedUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(mixedQueue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (MixedUser iteMixedUser : mixedQueue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteMixedUser.getName()).append(" (@").append(iteMixedUser.getUsername()).append(")\n");
            ++id;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(QueueMarkupUtil.createMarkup(mixedQueue));
        Message sendTextMessage = this.telegramClient.sendTextMessage(queueMessage);
        try {
            this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        } catch (TelegramApiException e) {
            sendMessage("Якщо ви хочете, щоб повідомлення було закріплено автоматично, надайте мені необхідні дозволи!", message);
        }
        mixedQueue.setMessageId(sendTextMessage.getMessageId());
        mixedQueue.setChatId(message.getChatId());
        queueService.save(mixedQueue);
    }
}

