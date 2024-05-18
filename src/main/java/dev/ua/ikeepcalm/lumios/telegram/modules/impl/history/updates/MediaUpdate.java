package dev.ua.ikeepcalm.lumios.telegram.modules.impl.history.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.UpdateParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Component
public class MediaUpdate extends UpdateParent {

    @Override
    public void processUpdate(Update update) {
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setText("MEDIA_UNDETERMINED_TYPE");
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
