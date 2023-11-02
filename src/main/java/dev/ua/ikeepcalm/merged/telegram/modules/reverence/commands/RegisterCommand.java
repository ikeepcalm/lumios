package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class RegisterCommand extends CommandParent {

    private final UserService userService;
    private final ChatService chatService;

    public RegisterCommand(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    public void execute(Message origin) {
        User user = origin.getFrom();
        ReverenceChat linkedChat = chatService.find(origin.getChatId());

        if (userService.checkIfUserExists(user.getId(), linkedChat)) {
            reply(origin, "Ваш аккаунт вже зареєстровано у цьому чаті!\nНемає необхідності робити це ще раз");
        } else {
            reply(origin, "Ваш аккаунт зареєстровано у цьому чаті!\nПриємної експлуатації, шкіряний мішок!");

            if (linkedChat == null) {
                ReverenceChat reverenceChat = new ReverenceChat();
                reverenceChat.setChatId(origin.getChatId());
                chatService.save(reverenceChat);
            }

            ReverenceUser reverenceUser = new ReverenceUser();
            reverenceUser.setUserId(user.getId());
            reverenceUser.setUsername(user.getUserName());
            reverenceUser.setCredits(100);
            reverenceUser.setSustainable(100);
            reverenceUser.setChannel(linkedChat);
            userService.save(reverenceUser);
        }
    }
}
