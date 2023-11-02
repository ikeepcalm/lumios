package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.ReverencePatterns;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class DecreaseCommand extends CommandParent {

    private final UserService userService;
    private final ChatService chatService;

    public DecreaseCommand(UserService userService, ChatService chatService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    public void execute(Message origin) {
        if (ReverencePatterns.isDecreaseCommand(origin)) {
            processDecreaseCommand(origin);
        } else {
            reply(origin, "Неправильний формат команди. Використовуйте: /decrease@queueupnow_bot [@юзернейм] [кількість поваги]");
        }
    }

    private void processDecreaseCommand(Message origin) {
        String[] parts = origin.getText().split(" ");
        String username = parts[1].replace("@", "");
        int eventValue = Integer.parseInt(parts[2]);
        ReverenceChat linkedChat = chatService.find(origin.getChatId());
        User user = origin.getFrom();

        if (!userService.checkIfUserExists(user.getId(), linkedChat)) {
            reply(origin, "Ви не берете участь у системі боту.\nСпробуйте /register@queueupnow_bot!");
            return;
        }

        ReverenceUser currentUser = userService.findById(user.getId(), linkedChat);
        ReverenceUser mentionedUser = userService.findByUsername(username, linkedChat);

        if (mentionedUser == null) {
            reply(origin, "Учасника системи із заданим юзернеймом не знайдено. Подивіться уважно, і спробуйте ще раз!");
            return;
        }

        if (currentUser.getUserId().equals(mentionedUser.getUserId())) {
            reply(origin, "Ви не можете зменшувати повагу самому собі.");
            return;
        }

        if (currentUser.getCredits() < eventValue) {
            reply(origin, "Недостатньо кредитів! Наявно: " + currentUser.getCredits() +
                    "\nДочекайтеся щоденного оновлення.\nДізнатися більше: /help@queueupnow_bot.");
        } else {
            decreaseReverence(origin, currentUser, mentionedUser, eventValue);
        }
    }

    private void decreaseReverence(Message origin, ReverenceUser currentUser, ReverenceUser mentionedUser, int eventValue) {
        currentUser.setCredits(currentUser.getCredits() - eventValue);
        mentionedUser.setReverence(mentionedUser.getReverence() - eventValue);
        userService.save(currentUser);
        userService.save(mentionedUser);
        reply(origin, "Ви успішно відняли " + eventValue + " витратних кредитів з рейтингу користувача!");
    }
}
