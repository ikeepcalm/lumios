package dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class IncreasingUpdate extends CommandParent {
    public void execute(Update origin) {
        User user = origin.getMessage().getFrom();
        User repliedUser = origin.getMessage().getReplyToMessage().getFrom();
        ReverenceChat linkedChatId = this.chatService.find(origin.getMessage().getChatId());
        if (dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.updates.IncreasingUpdate.isIncreasingUpdate(origin) && !user.equals(repliedUser) && !repliedUser.getIsBot()) {
            if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
                if (this.userService.checkIfUserExists(repliedUser.getId(), linkedChatId)) {
                    ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
                    ReverenceUser foundRepliedUser = this.userService.findById(repliedUser.getId(), linkedChatId);
                    int eventValue = Integer.parseInt(origin.getMessage().getText().replace("+", ""));
                    if (foundUser.getCredits() < eventValue) {
                        this.reply(origin.getMessage(), "Недостатньо кредитів! Наявно: " + foundUser.getCredits() + " \nДочекайтеся щоденного оновлення.\nДізнатися більше: /help@queueupnow_bot.");
                    } else if (foundUser.getCredits() > eventValue) {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() + eventValue);
                        foundUser.setCredits(foundUser.getCredits() - eventValue);
                        this.userService.save(foundUser);
                        this.userService.save(foundRepliedUser);
                        reply(origin.getMessage(), "✔⠀");
                    } else {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() + eventValue);
                        foundUser.setCredits(0);
                        this.userService.save(foundUser);
                        this.userService.save(foundRepliedUser);
                        reply(origin.getMessage(), "✔⠀");
                    }
                } else {
                    this.reply(origin.getMessage(), "Той, кому ви здійснили спробу змінити показник поваги, ще не бере участь у системі боту. Нехай спробує /register@queueupnow_bot!");
                }
            } else {
                this.reply(origin.getMessage(), "Ви не берете участь у системі боту.\nСпробуйте /register@queueupnow_bot!");
            }
        }
    }

}
