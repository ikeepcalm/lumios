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
@BotReaction
public class RemoveReactionUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        if (update.getMessageReaction() == null) {
            log.debug("MessageReaction is null, skipping");
            return;
        }
        
        try {
            MessageReactionUpdated message = update.getMessageReaction();
            log.debug("Processing remove reaction from user {} in chat {}, message ID: {}", 
                user.getUserId(), chat.getChatId(), message.getMessageId());
            
            ReverenceReaction removedReaction = findNewReaction(message.getNewReaction(), message.getOldReaction());
            int reactionValue = ReverenceReaction.determineReactionValue(removedReaction);
            
            log.debug("Removed reaction: {}, value: {}, user credits: {}", 
                removedReaction, reactionValue, user.getCredits());

            if (user.getCredits() >= Math.abs(reactionValue)) {
                LumiosUser onUser = recordService.findByMessageIdAndChatId(Long.valueOf(message.getMessageId()), message.getChat().getId()).getUser();
                if (onUser == null) {
                    log.warn("Target user not found for message ID: {} in chat: {}", message.getMessageId(), message.getChat().getId());
                    return;
                }
                
                if (!user.getUserId().equals(onUser.getUserId())) {
                    int oldCredits = user.getCredits();
                    int oldReverence = onUser.getReverence();
                    
                    user.setCredits(user.getCredits() - Math.abs(reactionValue));
                    if (reactionValue < 0) {
                        onUser.setReverence(onUser.getReverence() + reactionValue);
                    } else if (reactionValue > 0) {
                        onUser.setReverence(onUser.getReverence() - reactionValue);
                    }
                    
                    userService.save(user);
                    userService.save(onUser);
                    
                    log.info("Remove reaction processed: User {} (credits: {} -> {}) removed reaction from {} (reverence: {} -> {})", 
                        user.getUserId(), oldCredits, user.getCredits(), 
                        onUser.getUserId(), oldReverence, onUser.getReverence());
                } else {
                    log.debug("User {} tried to remove reaction from their own message", user.getUserId());
                }
            } else {
                log.warn("User {} has insufficient credits ({}) for removing reaction value {}", 
                    user.getUserId(), user.getCredits(), Math.abs(reactionValue));
            }
        } catch (NoSuchEntityException e) {
            log.warn("Message record not found for message ID: {} in chat: {}", 
                update.getMessageReaction().getMessageId(), update.getMessageReaction().getChat().getId());
        } catch (Exception e) {
            log.error("Unexpected error processing remove reaction", e);
        }
    }
}
