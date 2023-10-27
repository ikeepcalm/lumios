package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartCommand
        extends CommandParent {
    public void execute(Message origin) {
        if (this.chatService.find(origin.getChatId()) == null) {
            ReverenceChat reverenceChat = new ReverenceChat();
            reverenceChat.setChatId(origin.getChatId());
            this.chatService.save(reverenceChat);
            this.reply(origin, "Вітаю! Успішно зареєстровано цей чат у системі, приємного користування!");
        } else {
            this.reply(origin, "Цей чат вже зареєстровано у системі, немає необхідності робити це ще раз!");
        }
    }
}

