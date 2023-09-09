/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.User
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.reverence;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.patterns.CommandPatterns.DecreasingCommand;
import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class DecreaseCommand
extends Executable {
    public void execute(Message origin) {
        if (DecreasingCommand.isDecreaseCommand(origin)) {
            String string = origin.getText();
            String[] parts = string.split(" ");
            String username = parts[1].replace("@", "");
            int eventValue = Integer.parseInt(parts[2]);
            ReverenceChat linkedChatId = this.chatService.find(origin.getChatId());
            User user = origin.getFrom();
            if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
                if (this.userService.checkIfMentionedUserExists(username, linkedChatId)) {
                    ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
                    ReverenceUser foundRepliedUser = this.userService.findByUsername(username, linkedChatId);
                    if (!foundUser.getUserId().equals(foundRepliedUser.getUserId())) {
                        if (foundUser.getCredits() < eventValue) {
                            this.reply(origin, "\u041d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043d\u044c\u043e \u043a\u0440\u0435\u0434\u0438\u0442\u0456\u0432! \u041d\u0430\u044f\u0432\u043d\u043e: " + foundUser.getCredits() + " \n\u0414\u043e\u0447\u0435\u043a\u0430\u0439\u0442\u0435\u0441\u044f \u0449\u043e\u0434\u0435\u043d\u043d\u043e\u0433\u043e \u043e\u043d\u043e\u0432\u043b\u0435\u043d\u043d\u044f.\n\u0414\u0456\u0437\u043d\u0430\u0442\u0438\u0441\u044f \u0431\u0456\u043b\u044c\u0448\u0435: /help@queueupnow_bot.");
                        } else if (foundUser.getCredits() > eventValue) {
                            foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                            foundUser.setCredits(foundUser.getCredits() - eventValue);
                            this.userService.save(foundUser);
                            this.userService.save(foundRepliedUser);
                            this.reply(origin, "\u0412\u0438 \u0443\u0441\u043f\u0456\u0448\u043d\u043e \u0432\u0456\u0434\u043d\u044f\u043b\u0438 \u0432\u043a\u0430\u0437\u0430\u043d\u0443 \u043a\u0456\u043b\u044c\u043a\u0456\u0441\u0442\u044c \u0432\u0438\u0442\u0440\u0430\u0442\u043d\u0438\u0445 \u043a\u0440\u0435\u0434\u0438\u0442\u0456\u0432 \u0437 \u0440\u0435\u0439\u0442\u0438\u043d\u0433\u0443 \u043a\u043e\u0440\u0438\u0441\u0442\u0443\u0432\u0430\u0447\u0430!");
                        } else {
                            foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                            foundUser.setCredits(0);
                            this.userService.save(foundUser);
                            this.userService.save(foundRepliedUser);
                            this.reply(origin, "\u0412\u0438 \u0443\u0441\u043f\u0456\u0448\u043d\u043e \u0432\u0456\u0434\u043d\u044f\u043b\u0438 \u0432\u043a\u0430\u0437\u0430\u043d\u0443 \u043a\u0456\u043b\u044c\u043a\u0456\u0441\u0442\u044c \u0432\u0438\u0442\u0440\u0430\u0442\u043d\u0438\u0445 \u043a\u0440\u0435\u0434\u0438\u0442\u0456\u0432 \u0437 \u0440\u0435\u0439\u0442\u0438\u043d\u0433\u0443 \u043a\u043e\u0440\u0438\u0441\u0442\u0443\u0432\u0430\u0447\u0430!");
                        }
                    }
                } else {
                    this.reply(origin, "\u0423\u0447\u0430\u0441\u043d\u0438\u043a\u0430 \u0441\u0438\u0441\u0442\u0435\u043c\u0438 \u0456\u0437 \u0437\u0430\u0434\u0430\u043d\u0438\u043c \u044e\u0437\u0435\u0440\u043d\u0435\u0439\u043c\u043e\u043c \u043d\u0435 \u0437\u043d\u0430\u0439\u0434\u0435\u043d\u043e. \u041f\u043e\u0434\u0438\u0432\u0456\u0442\u044c\u0441\u044f \u0443\u0432\u0430\u0436\u043d\u043e, \u0456 \u0441\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0449\u0435 \u0440\u0430\u0437!");
                }
            } else {
                this.reply(origin, "\u0412\u0438 \u043d\u0435 \u0431\u0435\u0440\u0435\u0442\u0435 \u0443\u0447\u0430\u0441\u0442\u044c \u0443 \u0441\u0438\u0441\u0442\u0435\u043c\u0456 \u0431\u043e\u0442\u0443.\n\u0421\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 /register@queueupnow_bot!");
            }
        } else {
            this.reply(origin, "\u0417\u0434\u0430\u0454\u0442\u044c\u0441\u044f, \u0449\u043e \u0432\u0438 \u043f\u043e\u043c\u0438\u043b\u0438\u043b\u0438\u0441\u044f \u0434\u0435\u0441\u044c \u0443 \u0432\u0438\u043a\u043e\u0440\u0438\u0441\u0442\u0430\u043d\u043d\u0456 \u0446\u0456\u0454\u0457 \u043a\u043e\u043c\u0430\u043d\u0434\u0438. \u041f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u0438\u0439 \u043f\u0430\u0442\u0442\u0435\u0440\u043d:\n/decrease@queueupnow_bot [@ + \u044e\u0437\u0435\u0440\u043d\u0435\u0439\u043c \u0443\u0447\u0430\u0441\u043d\u0438\u043a\u0430] [\u043a\u0456\u043b\u044c\u043a\u0456\u0441\u0442\u044c \u043f\u043e\u0432\u0430\u0433\u0438]");
        }
    }
}

