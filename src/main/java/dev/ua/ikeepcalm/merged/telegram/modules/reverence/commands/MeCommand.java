package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class MeCommand extends CommandParent {

    private final UserService userService;
    private final ChatService chatService;

    public MeCommand(UserService userService, ChatService chatService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    public void execute(Message origin) {
        User user = origin.getFrom();
        ReverenceChat linkedChat = chatService.find(origin.getChatId());
        if (userService.checkIfUserExists(user.getId(), linkedChat)) {
            ReverenceUser foundUser = userService.findById(user.getId(), linkedChat);
            reply(origin, ParseMode.MARKDOWN, "```Власна-статистика" +
                    " ◈ Рейтинг: " + foundUser.getReverence() + "\n" +
                    " ◈ Кредити: " + foundUser.getCredits() + "\n" +
                    " ◈ Оновлення: " + foundUser.getSustainable() + "\n" +
                    " ◈ Гаманець: " + foundUser.getBalance() + "```");

        } else {
            reply(origin, "Ви не берете участь у системі боту. Спробуйте /register@queueupnow_bot!");
        }
    }
}
