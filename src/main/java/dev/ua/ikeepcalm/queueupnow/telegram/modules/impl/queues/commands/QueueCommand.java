package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class QueueCommand extends CommandParent {

    @Override
    @Transactional
    public void processUpdate(Message message) {
        SimpleQueue simpleQueue;
        if (!message.getText().equals("/queue") && !message.getText().equals("/queue@queueupnow_bot")) {
            String alias = message.getText()
                    .replace("/queue@queueupnow_bot ", "")
                    .replace("/queue ", "")
                    .toUpperCase();
            simpleQueue = new SimpleQueue(alias);
        } else {
            simpleQueue = new SimpleQueue();
        }
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setName(message.getFrom().getFirstName());
        simpleUser.setAccountId(message.getFrom().getId());
        simpleUser.setUsername(message.getFrom().getUserName());
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
        this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        simpleQueue.setMessageId(sendTextMessage.getMessageId());
        simpleQueue.setChatId(message.getChatId());
        queueService.save(simpleQueue);
    }
}

