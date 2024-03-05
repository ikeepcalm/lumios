package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.queues.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.queues.utils.QueueMarkupUtil;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MixedCommand extends CommandParent {

    @Override
    @Transactional
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        MixedQueue mixedQueue;
        if (!message.getText().equals("/mixed") && !message.getText().equals("/mixed@queueupnow_bot")) {
            String alias = message.getText()
                    .replace("/mixed@queueupnow_bot ", "")
                    .replace("/mixed ", "")
                    .toUpperCase();
            mixedQueue = new MixedQueue(alias);
        } else {
            mixedQueue = new MixedQueue();
        }
        MixedUser mixedUser = new MixedUser();
        mixedUser.setName(message.getFrom().getFirstName());
        mixedUser.setAccountId(message.getFrom().getId());
        mixedUser.setUsername(message.getFrom().getUserName());
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
        this.telegramClient.pinChatMessage(sendTextMessage.getChatId(), sendTextMessage.getMessageId());
        mixedQueue.setMessageId(sendTextMessage.getMessageId());
        queueService.save(mixedQueue);
    }
}

