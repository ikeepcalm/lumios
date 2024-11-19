package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

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
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@BotCommand(command = "queue")
public class QueueCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        SimpleQueue simpleQueue;
        if (!message.getText().equals("/queue") && !message.getText().equals("/queue@lumios_bot")) {
            String alias = message.getText()
                    .replace("/queue@lumios_bot ", "")
                    .replace("/queue ", "")
                    .toUpperCase();

            if (alias.isBlank() || alias.length() > 20) {
                sendMessage("Назва черги повинна бути від 1 до 20 символів!", message);
                return;
            }
            simpleQueue = new SimpleQueue(alias);
        } else {
            sendMessage("Введіть назву черги після команди! Черги без назви були поміченими застарілими починаючи із версії 2.0.0!", message);
            return;
        }

        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setName(message.getFrom().getFirstName());
        simpleUser.setAccountId(message.getFrom().getId());
        if (message.getFrom().getUserName() == null) {
            simpleUser.setUsername("ukhilyant");
        } else {
            simpleUser.setUsername(message.getFrom().getUserName());
        }
        simpleQueue.getContents().add(simpleUser);
        TextMessage queueMessage = new TextMessage();
        queueMessage.setChatId(message.getChatId());
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
        try {
            this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        } catch (TelegramApiException e) {
            sendMessage("Якщо ви хочете, щоб повідомлення було закріплено автоматично, надайте мені необхідні дозволи!", message);
        }
        simpleQueue.setMessageId(sendTextMessage.getMessageId());
        simpleQueue.setChatId(message.getChatId());
        queueService.save(simpleQueue);
    }
}

