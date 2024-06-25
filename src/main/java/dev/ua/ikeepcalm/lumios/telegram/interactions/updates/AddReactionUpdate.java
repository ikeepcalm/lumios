package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Component
@BotUpdate
public class AddReactionUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        if (update.getMessageReaction() == null) return;
        try {
            MessageReactionUpdated message = update.getMessageReaction();
            ReverenceReaction newReaction = findNewReaction(message.getOldReaction(), message.getNewReaction());
            int reactionValue = ReverenceReaction.determineReactionValue(newReaction);

            if (user.getCredits() > reactionValue) {
                LumiosUser onUser = recordService.findByMessageIdAndChatId(Long.valueOf(message.getMessageId()), message.getChat().getId()).getUser();
                if (!user.getUsername().equals(onUser.getUsername())) {
                    user.setCredits(user.getCredits() - Math.abs(reactionValue));
                    onUser.setReverence(onUser.getReverence() + reactionValue);
                    userService.save(user);
                    userService.save(onUser);
                }
            }
        } catch (NoSuchEntityException ignored) {
        }
    }

}

