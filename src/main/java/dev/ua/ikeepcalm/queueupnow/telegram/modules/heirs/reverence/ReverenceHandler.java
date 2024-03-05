package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.HandlerParent;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.commands.MeCommand;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.commands.StatsCommand;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.updates.AddReactionUpdate;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.updates.RemoveReactionUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;

@Component
public class ReverenceHandler implements HandlerParent {

    private final AddReactionUpdate addReactionUpdate;
    private final RemoveReactionUpdate removeReactionUpdate;
    private final MeCommand meCommand;
    private final StatsCommand statsCommand;

    public ReverenceHandler(AddReactionUpdate addReactionUpdate,
                            RemoveReactionUpdate removeReactionUpdate,
                            MeCommand meCommand, StatsCommand statsCommand) {
        this.addReactionUpdate = addReactionUpdate;
        this.removeReactionUpdate = removeReactionUpdate;
        this.meCommand = meCommand;
        this.statsCommand = statsCommand;
    }


    @Override
    public void dispatchUpdate(Update update) {
        if (update.getMessageReaction() != null){
            MessageReactionUpdated reactionUpdated = update.getMessageReaction();
            int oldCount = reactionUpdated.getOldReaction().size();
            int newCount = reactionUpdated.getNewReaction().size();
            if (oldCount < newCount){
                addReactionUpdate.processUpdate(update);
            } else if (oldCount > newCount){
                removeReactionUpdate.processUpdate(update);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()){
            String commandText = update.getMessage().getText();
            String[] parts = commandText.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            command = command.replace("@queueupnow_bot", "");
            switch (command) {
                case "/me" -> meCommand.processUpdate(update.getMessage());
                case "/stats", "/rating" -> statsCommand.processUpdate(update.getMessage());
            }
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.getMessageReaction() != null){
            return true;
        } else {
            if (update.hasMessage() && update.getMessage().hasText()){
                return update.getMessage().getText().startsWith("/");
            } else {
                 return update.hasCallbackQuery();
            }
        }
    }
}
