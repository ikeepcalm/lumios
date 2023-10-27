package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class RegisterCommand
        extends CommandParent {
    public void execute(Message origin) {
        User user = origin.getFrom();
        if (this.userService.checkIfUserExists(user.getId(), this.chatService.find(origin.getChatId()))) {
            this.reply(origin, "Ваш аккаунт вже зарeєстровано у цьому чаті!\nНемає необхідності робити це ще раз");
        } else {
            this.reply(origin, "Ваш аккаунт зарeєстровано у цьому чаті!\nПриємної експлуатації, шкіряний мішок!");
            if (this.chatService.find(origin.getChatId()) == null) {
                ReverenceChat reverenceChat = new ReverenceChat();
                reverenceChat.setChatId(origin.getChatId());
                this.chatService.save(reverenceChat);
            }
            ReverenceUser reverenceUser = new ReverenceUser();
            reverenceUser.setUserId(user.getId());
            reverenceUser.setUsername(user.getUserName());
            reverenceUser.setCredits(100);
            reverenceUser.setSustainable(100);
            reverenceUser.setChannel(this.chatService.find(origin.getChatId()));
            this.userService.save(reverenceUser);
        }
    }
}

