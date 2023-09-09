/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Update
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.updates;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class AdditionUpdate
extends Executable {
    public void execute(Update origin) {
        User user = origin.getMessage().getFrom();
        User repliedUser = origin.getMessage().getReplyToMessage().getFrom();
        ReverenceChat linkedChatId = this.chatService.find(origin.getMessage().getChatId());
        if (dev.ua.ikeepcalm.merged.patterns.UpdatePatterns.AdditionUpdate.isAdditionUpdate(origin) && !user.equals((Object)repliedUser) && !repliedUser.getIsBot().booleanValue()) {
            if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
                if (this.userService.checkIfUserExists(repliedUser.getId(), linkedChatId)) {
                    ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
                    ReverenceUser foundRepliedUser = this.userService.findById(repliedUser.getId(), linkedChatId);
                    int eventValue = Integer.parseInt(origin.getMessage().getText().replace("+", ""));
                    if (foundUser.getCredits() < eventValue) {
                        this.reply(origin.getMessage(), "\u041d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u043a\u0440\u0435\u0434\u0438\u0442\u0456\u0432! \u041d\u0430\u044f\u0432\u043d\u043e: " + foundUser.getCredits() + " \n\u0414\u043e\u0447\u0435\u043a\u0430\u0439\u0442\u0435\u0441\u044f \u0449\u043e\u0434\u0435\u043d\u043d\u043e\u0433\u043e \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f.\n\u0414\u0456\u0437\u043d\u0430\u0442\u0438\u0441\u044f \u0431\u0456\u043b\u044c\u0448\u0435: /help@queueupnow_bot.");
                    } else if (foundUser.getCredits() > eventValue) {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() + eventValue);
                        foundUser.setCredits(foundUser.getCredits() - eventValue);
                        this.userService.save(foundUser);
                        this.userService.save(foundRepliedUser);
                        this.reply(origin.getMessage(), "\u2714\u2800");
                    } else {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() + eventValue);
                        foundUser.setCredits(0);
                        this.userService.save(foundUser);
                        this.userService.save(foundRepliedUser);
                        this.reply(origin.getMessage(), "\u2714\u2800");
                    }
                } else {
                    this.reply(origin.getMessage(), "\u0422\u043e\u0439, \u043a\u043e\u043c\u0443 \u0432\u0438 \u0437\u0434\u0456\u0439\u0441\u043d\u0438\u043b\u0438 \u0441\u043f\u0440\u043e\u0431\u0443 \u0437\u043c\u0456\u043d\u0438\u0442\u0438 \u043f\u043e\u043a\u0430\u0437\u043d\u0438\u043a \u043f\u043e\u0432\u0430\u0433\u0438, \u0449\u0435 \u043d\u0435 \u0431\u0435\u0440\u0435 \u0443\u0447\u0430\u0441\u0442\u044c \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456 \u0431\u043e\u0442\u0443. \u041d\u0435\u0445\u0430\u0439 \u0441\u043f\u0440\u043e\u0431\u0443\u0454 /register@queueupnow_bot!");
                }
            } else {
                this.reply(origin.getMessage(), "\u0412\u0438 \u043d\u0435 \u0431\u0435\u0440\u0435\u0442\u0435 \u0443\u0447\u0430\u0441\u0442\u044c \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456 \u0431\u043e\u0442\u0443.\n\u0421\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 /register@queueupnow_bot!");
            }
        }
    }
}

