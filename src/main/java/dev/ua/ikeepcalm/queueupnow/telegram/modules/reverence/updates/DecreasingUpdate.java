package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.updates;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.patterns.ReverencePatterns;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class DecreasingUpdate extends CommandParent {

    @Override
    @SneakyThrows
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        if (ReverencePatterns.isDecreasingUpdate(message) && userService.checkIfUserExists(message.getReplyToMessage().getFrom().getId(), reverenceChat)) {
            if (!message.getFrom().getUserName().equals(message.getReplyToMessage().getFrom().getUserName())) {
                ReverenceUser foundRepliedUser = userService.findById(message.getReplyToMessage().getFrom().getId(), reverenceChat);
                int eventValue = Math.abs(Integer.parseInt(message.getText()));
                if (reverenceUser.getCredits() >= eventValue) {
                    foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                    reverenceUser.setCredits(reverenceUser.getCredits() - eventValue);
                    userService.save(foundRepliedUser);
                    userService.save(reverenceUser);
                    sendMessage("✔");
                } else {
                    sendMessage("Недостатньо кредитів! Дочекайтеся щоденного оновлення\nДізнатися більше: /help@queueupnow_bot.");
                }
            } else {
                sendMessage("Ви не можете зменшувати повагу самому собі!");
            }
        }
    }

}
