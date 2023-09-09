/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
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
            this.reply(origin, "\u0412\u0456\u0442\u0430\u044e! \u0423\u0441\u043f\u0456\u0448\u043d\u043e \u0437\u0430\u0440\u0435\u0454\u0441\u0442\u0440\u043e\u0432\u0430\u043d\u043e \u0446\u0435\u0439 \u0447\u0430\u0442 \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456, \u043f\u0440\u0438\u0454\u043c\u043d\u043e\u0433\u043e \u043a\u043e\u0440\u0438\u0441\u0442\u0443\u0432\u0430\u043d\u043d\u044f!");
        } else {
            this.reply(origin, "\u0426\u0435\u0439 \u0447\u0430\u0442 \u0432\u0436\u0435 \u0437\u0430\u0440\u0435\u0454\u0441\u0442\u0440\u043e\u0432\u0430\u043d\u043e \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456, \u043d\u0435\u043c\u0430\u0454 \u043d\u0435\u043e\u0431\u0445\u0456\u0434\u043d\u043e\u0441\u0442\u0456 \u0440\u043e\u0431\u0438\u0442\u0438 \u0446\u0435 \u0449\u0435 \u0440\u0430\u0437!");
        }
    }
}

