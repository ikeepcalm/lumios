package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.patterns.ReverencePatterns;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class DecreaseCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        if (ReverencePatterns.isDecreaseCommand(message)) {
            processDecreaseCommand(message);
        } else {
            sendMessage("Неправильний формат команди. Використовуйте: /decrease@queueupnow_bot [@юзернейм] [кількість поваги]");
        }
    }

    private void processDecreaseCommand(Message message) {
        String[] parts = message.getText().split(" ");
        String username = parts[1].replace("@", "");
        int eventValue = Integer.parseInt(parts[2]);
        ReverenceUser mentionedUser = userService.findByUsername(username, reverenceChat);

        if (mentionedUser == null) {
            sendMessage("Учасника системи із заданим юзернеймом не знайдено. Подивіться уважно, і спробуйте ще раз!");
            return;
        }

        if (reverenceUser.getUserId().equals(mentionedUser.getUserId())) {
            sendMessage("Ви не можете зменшувати повагу самому собі.");
            return;
        }

        if (reverenceUser.getCredits() < eventValue) {
            sendMessage("Недостатньо кредитів! Наявно: " + reverenceUser.getCredits() +
                    "\nДочекайтеся щоденного оновлення.\nДізнатися більше: /help@queueupnow_bot.");
        } else {
            decreaseReverence(reverenceUser, mentionedUser, eventValue);
        }
    }

    private void decreaseReverence(ReverenceUser currentUser, ReverenceUser mentionedUser, int eventValue) {
        currentUser.setCredits(currentUser.getCredits() - eventValue);
        mentionedUser.setReverence(mentionedUser.getReverence() - eventValue);
        userService.save(currentUser);
        userService.save(mentionedUser);
        sendMessage("✔");
    }


}
