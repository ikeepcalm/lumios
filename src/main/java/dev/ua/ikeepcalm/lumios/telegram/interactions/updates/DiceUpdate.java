package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.ReactionMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Dice;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import java.util.ArrayList;
import java.util.List;

@Component
@BotUpdate
public class DiceUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update) {
        if (update.getMessage().hasDice()) {
            Dice dice = update.getMessage().getDice();
            LumiosChat chat;
            try {
                chat = chatService.findByChatId(update.getMessage().getChatId());
            } catch (NoSuchEntityException e) {
                return;
            }

            if (chat.isDiceEnabled()) {
                LumiosUser user;
                try {
                    user = userService.findById(update.getMessage().getFrom().getId(), chat);
                } catch (NoSuchEntityException e) {
                    return;
                }
                double coefficient = 1;
                switch (dice.getEmoji()) {
                    case "\uD83C\uDFB0" -> {
                        coefficient = getSlotMachineCoefficient(dice.getValue());
                        user.setReverence((int) (user.getReverence() * coefficient));
                    }

                    case "\uD83C\uDFB2" -> {
                        coefficient = getDiceCoefficient(dice.getValue());
                        user.setReverence((int) (user.getReverence() * coefficient));
                    }
                }
                if (coefficient == 1) {
                    sendNeutralReaction(update.getMessage());
                } else if (coefficient > 1) {
                    sendPositiveReaction(update.getMessage());
                } else {
                    sendNegativeReaction(update.getMessage());
                }

                scheduleMessageToDelete(update.getMessage());
                userService.save(user);
            }
        }
    }


    private double getDiceCoefficient(int diceValue) {
        return switch (diceValue) {
            case 1 -> 0.5;
            case 2 -> 0.6;
            case 3 -> 0.9;
            case 4 -> 1.5;
            case 5 -> 1.8;
            case 6 -> 2.0;
            default -> throw new IllegalArgumentException("Invalid dice value: " + diceValue);
        };
    }

    private double getSlotMachineCoefficient(int slotValue) {
        if (slotValue >= 1 && slotValue <= 10) {
            return 0.7;
        } else if (slotValue >= 11 && slotValue <= 20) {
            return 0.8;
        } else if (slotValue >= 21 && slotValue <= 30) {
            return 0.9;
        } else if (slotValue >= 31 && slotValue <= 40) {
            return 1;
        } else if (slotValue >= 41 && slotValue <= 50) {
            return 1.5;
        } else if (slotValue >= 51 && slotValue <= 60) {
            return 1.8;
        } else if (slotValue >= 61 && slotValue <= 63) {
            return 2.0;
        } else if (slotValue == 64) {
            return 2.5;
        } else {
            throw new IllegalArgumentException("Invalid slot machine value: " + slotValue);
        }
    }

    private void sendPositiveReaction(Message message) {
        ReactionMessage reactionMessage = new ReactionMessage();
        reactionMessage.setChatId(message.getChatId());
        reactionMessage.setMessageId(message.getMessageId());
        List<ReactionType> reactionTypes = new ArrayList<>();
        reactionTypes.add(new ReactionTypeEmoji(ReactionType.EMOJI_TYPE, "👾"));
        reactionMessage.setReactionTypes(reactionTypes);
        telegramClient.sendReaction(reactionMessage);
    }

    private void sendNegativeReaction(Message message) {
        ReactionMessage reactionMessage = new ReactionMessage();
        reactionMessage.setChatId(message.getChatId());
        reactionMessage.setMessageId(message.getMessageId());
        List<ReactionType> reactionTypes = new ArrayList<>();
        reactionTypes.add(new ReactionTypeEmoji(ReactionType.EMOJI_TYPE, "\uD83E\uDD2E"));
        reactionMessage.setReactionTypes(reactionTypes);
        telegramClient.sendReaction(reactionMessage);
    }

    private void sendNeutralReaction(Message message) {
        ReactionMessage reactionMessage = new ReactionMessage();
        reactionMessage.setChatId(message.getChatId());
        reactionMessage.setMessageId(message.getMessageId());
        List<ReactionType> reactionTypes = new ArrayList<>();
        reactionTypes.add(new ReactionTypeEmoji(ReactionType.EMOJI_TYPE, "🐳"));
        reactionMessage.setReactionTypes(reactionTypes);
        telegramClient.sendReaction(reactionMessage);
    }

}
