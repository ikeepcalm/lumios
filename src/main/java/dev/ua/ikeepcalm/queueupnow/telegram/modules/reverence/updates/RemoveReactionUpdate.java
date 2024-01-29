package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.updates;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.UpdateParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Component
public class RemoveReactionUpdate extends UpdateParent {

    @Override
    public void processUpdate(Update update) {
        instantiateUpdate(update);
        try {
            MessageReactionUpdated message = update.getMessageReaction();
            ReverenceReaction newReaction = findNewReaction(message.getOldReaction(), message.getNewReaction());
            int reactionValue = ReverenceReaction.determineReactionValue(newReaction);
            if (reverenceUser.getCredits() > reactionValue) {
                ReverenceUser onUser = recordService.findByMessageIdAndChatId(Long.valueOf(message.getMessageId()), message.getChat().getId()).getUser();
                if (reverenceUser != onUser) {
                    reverenceUser.setCredits(reverenceUser.getCredits() - Math.abs(reactionValue));
                    if (reactionValue < 0){
                        onUser.setReverence(onUser.getReverence() + reactionValue);
                    } else if (reactionValue > 0){
                        onUser.setReverence(onUser.getReverence() - reactionValue);
                    }
                    userService.save(reverenceUser);
                    userService.save(onUser);
                }
            }
        } catch (NoSuchEntityException ignored) {}
    }
}
