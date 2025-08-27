package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Component
@BotUpdate
public class TextUpdate extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update) {
        MessageRecord messageRecord = new MessageRecord();
        if (update.hasMessage() && update.getMessage().hasText()) {

//            if (update.getMessage().getText().length() > 30000)
//                messageRecord.setText(update.getMessage().getText().substring(1, 30000));
//            else {
//                messageRecord.setText(update.getMessage().getText());
//            }

            messageRecord.setText("REPLACED MESSAGE TEXT HERE");

            if (update.getMessage().isReply()) {
                messageRecord.setReplyToMessageId(Long.valueOf(update.getMessage().getReplyToMessage().getMessageId()));
            }
        } else {
            messageRecord.setText("MEDIA_UNDETERMINED_TYPE");
        }

        messageRecord.setChatId(update.getMessage().getChatId());
        messageRecord.setMessageId(Long.valueOf(update.getMessage().getMessageId()));
        try {
            messageRecord.setUser(userService.findById(update.getMessage().getFrom().getId(), chatService.findByChatId(update.getMessage().getChatId())));
        } catch (NoSuchEntityException ignored) {
            return;
        }
        messageRecord.setDate(LocalDateTime.now());
        recordService.save(messageRecord);
    }

}
