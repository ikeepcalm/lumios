package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.history.updates;

import dev.ua.ikeepcalm.queueupnow.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.UpdateParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TextUpdate extends UpdateParent {
    @Override
    public void processUpdate(Update update) {
        instantiateUpdate(update);
        MessageRecord messageRecord = new MessageRecord();
        if (update.getMessage().hasText() && update.getMessage().getText().length() > 30000)
            messageRecord.setText(update.getMessage().getText().substring(1, 30000));
        else
            messageRecord.setText(update.getMessage().getText());
        messageRecord.setChatId(update.getMessage().getChatId());
        messageRecord.setMessageId(Long.valueOf(update.getMessage().getMessageId()));
        messageRecord.setUser(reverenceUser);
        recordService.save(messageRecord);
    }

}
