package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.commands.DecreasingCommand;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class DecreaseCommand
extends CommandParent {
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
                            this.reply(origin, "Недостатньо кредитів! Наявно: " + foundUser.getCredits() + " \nДочекайтеся щоденного оновлення.\nДізнатися більше: /help@queueupnow_bot.");
                        } else if (foundUser.getCredits() > eventValue) {
                            foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                            foundUser.setCredits(foundUser.getCredits() - eventValue);
                            this.userService.save(foundUser);
                            this.userService.save(foundRepliedUser);
                            this.reply(origin, "Ви успішно відняли вказану кількість витратних кредитів з рейтингу користувача!");
                        } else {
                            foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                            foundUser.setCredits(0);
                            this.userService.save(foundUser);
                            this.userService.save(foundRepliedUser);
                            this.reply(origin, "Ви успішно відняли вказану кількість витратних кредитів з рейтингу користувача!");
                        }
                    }
                } else {
                    this.reply(origin, "Учасника системи із заданим юзернеймом не знайдено. Подивіться уважно, і спробуйте ще раз!");
                }
            } else {
                this.reply(origin, "Ви не берете участь у системі боту.\nСпробуйте /register@queueupnow_bot!");
            }
        } else {
            this.reply(origin, "Здається, що ви помилилися десь у використанні цієї команди. Правильний паттерн:\n/decrease@queueupnow_bot [@ + юзернейм учасника] [кількість поваги]");
        }
    }
}

