package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class EveryoneCommand
extends CommandParent {
    public void execute(Message origin) {
        Message message = sendMessage(origin, "@" + origin.getFrom().getUserName() + " покликав вас до чату!");
        absSender.pinChatMessage(message.getChatId(), origin.getMessageId());
    }
}

