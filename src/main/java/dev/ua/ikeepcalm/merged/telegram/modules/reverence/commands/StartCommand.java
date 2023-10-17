/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartCommand
extends Executable {
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

