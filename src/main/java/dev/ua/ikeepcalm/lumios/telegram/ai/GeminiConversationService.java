package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiConversationService {

    private static final int MAX_CONTEXT_MESSAGES = 10;
    private final RecordService recordService;

    public GeminiConversationService(RecordService recordService) {
        this.recordService = recordService;
    }

    public JSONArray getConversationContext(Long chatId) {
        List<MessageRecord> recentMessages = recordService.findLastMessagesByChatId(chatId, MAX_CONTEXT_MESSAGES);
        JSONArray context = new JSONArray();
        
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            MessageRecord message = recentMessages.get(i);
            boolean isBot = message.getUser() == null;
            
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", isBot ? "model" : "user");
            
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", message.getText());
            parts.put(textPart);
            
            messageObj.put("parts", parts);
            context.put(messageObj);
        }
        
        return context;
    }
    
    public JSONArray getReplyChainContext(Long chatId, Long messageId) {
        List<MessageRecord> replyChain = recordService.findAllInReplyChain(chatId, messageId);
        JSONArray context = new JSONArray();
        
        for (int i = replyChain.size() - 1; i >= 0; i--) {
            MessageRecord message = replyChain.get(i);
            boolean isBot = message.getUser() == null;
            
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", isBot ? "model" : "user");
            
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", message.getText());
            parts.put(textPart);
            
            messageObj.put("parts", parts);
            context.put(messageObj);
        }
        
        return context;
    }
}