package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class BotDetectionUtils {
    
    public static boolean isBotMentionedInText(String textMessage, String botName, LumiosChat chat) {
        if (textMessage == null || textMessage.isEmpty()) {
            return false;
        }
        
        boolean mentionedByName = textMessage.matches(".*\\B" + botName + "\\b.*");
        
        boolean mentionedByNickname = chat != null && chat.getBotNickname() != null &&
                !chat.getBotNickname().trim().isEmpty() && 
                textMessage.toLowerCase().contains(chat.getBotNickname().toLowerCase());
                
        return mentionedByName || mentionedByNickname;
    }
    
    public static boolean isReplyToBot(Message message, String botName) {
        return message.isReply() &&
                message.getReplyToMessage().getFrom().getIsBot() &&
                message.getReplyToMessage().getFrom().getUserName().equals(botName.replace("@", ""));
    }
}