package dev.ua.ikeepcalm.lumios.telegram.interactions.reactions;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.ReverenceReaction;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotReaction;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Slf4j
@Component
@BotReaction(isPlus = true)
public class AddReactionUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        if (update.getMessageReaction() == null) {
            return;
        }

        try {
            MessageReactionUpdated message = update.getMessageReaction();

            ReverenceReaction newReaction = findNewReaction(message.getOldReaction(), message.getNewReaction());
            int reactionValue = ReverenceReaction.determineReactionValue(newReaction);

            if (user.getCredits() >= Math.abs(reactionValue)) {
                LumiosUser onUser = recordService.findByMessageIdAndChatId(Long.valueOf(message.getMessageId()), message.getChat().getId()).getUser();
                if (onUser == null) {
                    return;
                }

                if (!user.getUserId().equals(onUser.getUserId())) {
                    user.setCredits(user.getCredits() - Math.abs(reactionValue));
                    onUser.setReverence(onUser.getReverence() + reactionValue);

                    userService.save(user);
                    userService.save(onUser);
                }
            }
        } catch (NoSuchEntityException e) {
            log.warn("Message record not found for message ID: {} in chat: {}",
                    update.getMessageReaction().getMessageId(), update.getMessageReaction().getChat().getId());
        } catch (Exception e) {
            log.error("Unexpected error processing add reaction", e);
        }
    }

}

