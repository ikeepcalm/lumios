package dev.ua.ikeepcalm.queueupnow.telegram.executing.callbacks;

import dev.ua.ikeepcalm.queueupnow.entities.Queue;
import dev.ua.ikeepcalm.queueupnow.entities.User;
import dev.ua.ikeepcalm.queueupnow.managers.QueueManager;
import dev.ua.ikeepcalm.queueupnow.telegram.executing.Executable;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.queueupnow.utils.DublicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.UUID;

@Component
public class JoinCallback extends Executable {

    @Autowired
    private QueueManager queueManager;

    private void updateMessage(Message origin, Queue queue) {
        AlterMessage generalMessage = new AlterMessage();
        generalMessage.setChatId(origin.getChatId());
        generalMessage.setMessageId((int) queue.getMessageId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ").append(queue.getAlias()).append(" <<<\n\n");
        int id = 1;
        for (User iteUser : queue.getContents()) {
            stringBuilder.append("ID: ").append(id).append(" - ").append(iteUser.getName()).append(" (@").append(iteUser.getUsername()).append(")\n");
            id++;
        }
        generalMessage.setText(stringBuilder.toString());
        generalMessage.setReplyKeyboard(DublicateUtil.createMarkup(queue));
        queue.setMessageId(telegramService.sendAlterMessage(generalMessage).getMessageId());
        queueManager.updateQueue(queue);
    }


    public void manage(String receivedCallback, CallbackQuery origin) {
        Queue queue = queueManager.getQueue(UUID.fromString(receivedCallback));

        User user = new User();
        user.setName(origin.getFrom().getFirstName());
        user.setAccountId(origin.getFrom().getId());
        user.setUsername(origin.getFrom().getUserName());

        if (!queue.getContents().contains(user)) {
            queue.addUser(user);
            updateMessage(origin.getMessage(), queue);
            telegramService.sendAnswerCallbackQuery("Успішно заброньовано місце у черзі!", origin.getId());
        } else {
            telegramService.sendAnswerCallbackQuery("Ви вже знаходитесь у цій черзі!", origin.getId());
        }
    }
}
