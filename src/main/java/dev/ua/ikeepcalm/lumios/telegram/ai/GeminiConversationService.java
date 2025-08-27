package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiConversationService {

    private static final int MAX_CONTEXT_MESSAGES = 5;
    private static final int MAX_MESSAGE_LENGTH = 500;
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

            if (message.getText() == null || message.getText().trim().isEmpty()) {
                continue;
            }

            JSONObject messageObj = new JSONObject();
            messageObj.put("role", isBot ? "model" : "user");

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();

            String messageText = message.getText();
            if (messageText.length() > MAX_MESSAGE_LENGTH) {
                messageText = messageText.substring(0, MAX_MESSAGE_LENGTH);
            }

            textPart.put("text", messageText);
            parts.put(textPart);

            messageObj.put("parts", parts);
            context.put(messageObj);
        }

        return context;
    }

    public JSONArray getReplyChainContext(Long chatId, Long messageId) {
        List<MessageRecord> replyChain = recordService.findAllInReplyChain(chatId, messageId);
        JSONArray context = new JSONArray();

        int maxChainLength = Math.min(replyChain.size(), MAX_CONTEXT_MESSAGES);

        for (int i = 0; i < maxChainLength; i++) {
            MessageRecord message = replyChain.get(i);
            boolean isBot = message.getUser() == null;

            if (message.getText() == null || message.getText().trim().isEmpty()) {
                continue;
            }

            JSONObject messageObj = new JSONObject();
            messageObj.put("role", isBot ? "model" : "user");

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();

            String messageText = message.getText();
            if (messageText.length() > MAX_MESSAGE_LENGTH) {
                messageText = messageText.substring(0, MAX_MESSAGE_LENGTH);
            }

            textPart.put("text", messageText);
            parts.put(textPart);

            messageObj.put("parts", parts);
            context.put(messageObj);
        }

        return context;
    }
}