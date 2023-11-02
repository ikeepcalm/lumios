package dev.ua.ikeepcalm.merged.telegram.modules.reverence.updates;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.ReverencePatterns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class IncreasingUpdate extends CommandParent {

    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    public IncreasingUpdate(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    public void execute(Update origin) {
        User user = origin.getMessage().getFrom();
        User repliedUser = origin.getMessage().getReplyToMessage().getFrom();
        ReverenceChat linkedChatId = chatService.find(origin.getMessage().getChatId());

        if (ReverencePatterns.isIncreasingUpdate(origin) && isValidUsers(user, repliedUser, linkedChatId)) {
            ReverenceUser foundUser = userService.findById(user.getId(), linkedChatId);
            ReverenceUser foundRepliedUser = userService.findById(repliedUser.getId(), linkedChatId);
            int eventValue = Math.abs(Integer.parseInt(origin.getMessage().getText().replace("+", "")));
            if (foundUser.getCredits() >= eventValue) {
                foundRepliedUser.setReverence(foundRepliedUser.getReverence() + eventValue);
                foundUser.setCredits(foundUser.getCredits() - eventValue);
            } else {
                foundRepliedUser.setReverence(foundRepliedUser.getReverence() + foundUser.getCredits());
                foundUser.setCredits(0);
            }
            userService.save(foundUser);
            userService.save(foundRepliedUser);
            reply(origin.getMessage(), "✔");
        } else {
            reply(origin.getMessage(), "✖\uFE0F");
        }
    }

    private boolean isValidUsers(User user, User repliedUser, ReverenceChat linkedChatId) {
        return !user.equals(repliedUser) && !repliedUser.getIsBot() &&
                userService.checkIfUserExists(user.getId(), linkedChatId) &&
                userService.checkIfUserExists(repliedUser.getId(), linkedChatId);
    }
}
