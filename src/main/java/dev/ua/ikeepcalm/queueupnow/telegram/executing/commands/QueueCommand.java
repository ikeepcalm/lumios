package dev.ua.ikeepcalm.queueupnow.telegram.executing.commands;

import dev.ua.ikeepcalm.queueupnow.entities.Queue;
import dev.ua.ikeepcalm.queueupnow.entities.User;
import dev.ua.ikeepcalm.queueupnow.managers.QueueManager;
import dev.ua.ikeepcalm.queueupnow.telegram.executing.Executable;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.queueupnow.utils.DublicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class QueueCommand extends Executable {

    @Autowired
    private QueueManager queueManager;

    public void execute(Message origin) {
        Queue queue;
        if (!origin.getText().equals("/queue")) {
            String alias = origin.getText().replace("/queue ", "").toUpperCase();
            queue = queueManager.createQueue(origin.getChatId(), alias);
        } else {
            queue = queueManager.createQueue(origin.getChatId());
        }

        User user = new User();
        user.setName(origin.getFrom().getFirstName());
        user.setAccountId(origin.getFrom().getId());
        user.setUsername(origin.getFrom().getUserName());

        queue.addUser(user);

        MultiMessage queueMessage = new MultiMessage();
        queueMessage.setChatId(origin.getChatId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (User iteUser : queue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteUser.getName()).append(" (@").append(iteUser.getUsername()).append(")\n");
            id++;
        }
        queueMessage.setText(stringBuilder.toString());
        queueMessage.setReplyKeyboard(DublicateUtil.createMarkup(queue));
        Message message = telegramService.sendMultiMessage(queueMessage);
        telegramService.pinChatMessage(origin.getChatId(), message.getMessageId());
        queue.setMessageId(message.getMessageId());
    }
}