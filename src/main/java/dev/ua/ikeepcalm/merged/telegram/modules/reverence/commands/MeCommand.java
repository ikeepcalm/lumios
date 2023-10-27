package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class MeCommand
        extends CommandParent {
    public void execute(Message origin) {
        User user = origin.getFrom();
        ReverenceChat linkedChatId = this.chatService.find(origin.getChatId());
        if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
            ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
            this.reply(origin, "↣ Ваша статистика ↢\n――――――――――\n◈ Рейтинг: " + foundUser.getReverence() + "\n◈ Кредити: " + foundUser.getCredits() + "\n◈ Оновлення: " + foundUser.getSustainable() + "\n◈ Гаманець: " + foundUser.getBalance() + "\n――――――――――\n↣ Ваша статистика ↢");
        } else {
            this.reply(origin, "Ви не берете участь у системі боту. Спробуйте /register@queueupnow_bot!");
        }
    }
}

