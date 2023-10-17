package dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.Executable;
import dev.ua.ikeepcalm.merged.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class DecreasingUpdate
extends Executable {
    public void execute(Update origin) {
        User user = origin.getMessage().getFrom();
        User repliedUser = origin.getMessage().getReplyToMessage().getFrom();
        ReverenceChat linkedChatId = this.chatService.find(origin.getMessage().getChatId());
        if (dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.updates.DecreasingUpdate.isDecreasingUpdate(origin) && !user.equals(repliedUser) && !repliedUser.getIsBot()) {
            if (this.userService.checkIfUserExists(user.getId(), linkedChatId)) {
                if (this.userService.checkIfUserExists(repliedUser.getId(), linkedChatId)) {
                    ReverenceUser foundUser = this.userService.findById(user.getId(), linkedChatId);
                    ReverenceUser foundRepliedUser = this.userService.findById(repliedUser.getId(), linkedChatId);
                    int eventValue = Integer.parseInt(origin.getMessage().getText().replace("-", ""));
                    if (foundUser.getCredits() < eventValue) {
                        reply(origin.getMessage(), "Недостатньо кредитів! Наявно: " + foundUser.getCredits() + "\nДочекайтеся щоденного оновлення.\nДізнатися більше: /help@queueupnow_bot.");
                    } else if (foundUser.getCredits() > eventValue) {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                        foundUser.setCredits(foundUser.getCredits() - eventValue);
                        userService.save(foundUser);
                        userService.save(foundRepliedUser);
                        Message reply = reply(origin.getMessage(), "✔⠀");
                        replyAndSchedule(origin, reply);
                    } else {
                        foundRepliedUser.setReverence(foundRepliedUser.getReverence() - eventValue);
                        foundUser.setCredits(0);
                        userService.save(foundUser);
                        userService.save(foundRepliedUser);
                        Message reply = reply(origin.getMessage(), "✔⠀");
                        replyAndSchedule(origin, reply);
                    }
                } else {
                    this.reply(origin.getMessage(), "Той, кому ви здійснили спробу змінити показник поваги, ще не бере участь у системі боту. Нехай спробує /register@queueupnow_bot!");
                }
            } else {
                this.reply(origin.getMessage(), "Ви не берете участь у системі боту.\nСпробуйте /register@queueupnow_bot!");
            }
        }
    }

    private void replyAndSchedule(Update origin, Message reply) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RemoveMessage purgeAction = new RemoveMessage(origin.getMessage().getMessageId(), origin.getMessage().getChatId());
                        RemoveMessage purgeResponse = new RemoveMessage(reply.getMessageId(), reply.getChatId());
                        absSender.sendRemoveMessage(purgeResponse);
                        absSender.sendRemoveMessage(purgeAction);
                    }
                }, 300000 );
    }
}

