package dev.ua.ikeepcalm.lumios.telegram.interactions.updates;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotUpdate;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.BotDetectionUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Component
@BotUpdate
public class TextUpdate extends ServicesShortcut implements Interaction {

    private String botName;

    public TextUpdate(Environment environment) {
        this.botName = environment.getProperty("TELEGRAM_USERNAME");
    }

    @Override
    public void fireInteraction(Update update) {
        if (update.hasMessage()) {
            try {
                LumiosChat chat = chatService.findByChatId(update.getMessage().getChatId());
                
                boolean isBotMentioned = update.getMessage().hasText() && 
                        BotDetectionUtils.isBotMentionedInText(update.getMessage().getText(), botName, chat);
                        
                boolean isReplyToBot = BotDetectionUtils.isReplyToBot(update.getMessage(), botName);

                if (isBotMentioned || isReplyToBot) {
                    return;
                }
            } catch (NoSuchEntityException e) {
                boolean isBotMentioned = update.getMessage().hasText() &&
                        BotDetectionUtils.isBotMentionedInText(update.getMessage().getText(), botName, null);
                        
                boolean isReplyToBot = BotDetectionUtils.isReplyToBot(update.getMessage(), botName);

                if (isBotMentioned || isReplyToBot) {
                    return;
                }
            }
        }

        MessageRecord messageRecord = new MessageRecord();
        if (update.hasMessage() && update.getMessage().hasText()) {
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
