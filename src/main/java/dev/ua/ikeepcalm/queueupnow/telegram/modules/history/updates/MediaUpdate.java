package dev.ua.ikeepcalm.queueupnow.telegram.modules.history.updates;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.UpdateParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MediaUpdate extends UpdateParent {

    @Override
    public void processUpdate(Update update) {
        instantiateUpdate(update);
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setText("MEDIA_UNDETERMINED_TYPE");
        messageRecord.setChatId(update.getMessage().getChatId());
        messageRecord.setMessageId(Long.valueOf(update.getMessage().getMessageId()));
        messageRecord.setUser(super.reverenceUser);
        recordService.save(messageRecord);
    }
}
