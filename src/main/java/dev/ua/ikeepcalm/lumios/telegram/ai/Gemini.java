package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class Gemini {

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKey;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final GeminiConversationService conversationService;
    private final RecordService recordService;

    public Gemini(GeminiConversationService conversationService, RecordService recordService) {
        this.conversationService = conversationService;
        this.recordService = recordService;
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId) {
        return getChatResponse(inputText, chatId, null, null);
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId, byte[] imageData) {
        return getChatResponse(inputText, chatId, imageData, null);
    }

    public CompletableFuture<String> getChatResponseForReply(String inputText, Long chatId, Long replyToMessageId) {
        return getChatResponse(inputText, chatId, null, replyToMessageId);
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId, byte[] imageData, Long replyToMessageId) {
        return CompletableFuture.supplyAsync(() -> {
            for (String key : apiKey) {
                try {
                    JSONObject jsonPayload = getJsonObject(inputText, chatId, imageData, replyToMessageId);

                    URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-002:generateContent?key=" + key);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(jsonPayload.toString().getBytes());
                        os.flush();
                    }

                    StringBuilder response = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }

                    String responseText = extractTextFromResponse(response.toString());

                    // Save response to database for future context
                    saveResponseToDatabase(chatId, responseText, replyToMessageId);

                    return responseText;

                } catch (Exception e) {
                    log.error("Error with key {}: {}", key, e.getMessage());
                }
            }
            throw new RuntimeException("Failed to get response from Gemini with all provided API keys");
        }, executorService);
    }

    private void saveResponseToDatabase(Long chatId, String responseText, Long replyToMessageId) {
        MessageRecord messageRecord = new MessageRecord();
        messageRecord.setChatId(chatId);
        messageRecord.setText(responseText);
        messageRecord.setDate(LocalDateTime.now());
        messageRecord.setReplyToMessageId(replyToMessageId);
        // Bot messages don't have a user, so leave user null
        // messageId will be set by the bot when it actually sends the message

        recordService.save(messageRecord);
    }

    @NotNull
    private JSONObject getJsonObject(String inputText, Long chatId, byte[] imageData, Long replyToMessageId) {
        JSONObject jsonPayload = new JSONObject();

        // Get conversation context from database
        JSONArray contentsArray;
        if (replyToMessageId != null) {
            contentsArray = conversationService.getReplyChainContext(chatId, replyToMessageId);
        } else {
            contentsArray = conversationService.getConversationContext(chatId);
        }

        // Add current user message
        JSONObject userObject = new JSONObject();
        userObject.put("role", "user");
        JSONArray userPartsArray = new JSONArray();

        // Add text part
        JSONObject userTextPart = new JSONObject();
        userTextPart.put("text", inputText);
        userPartsArray.put(userTextPart);

        // Add image part if available
        if (imageData != null) {
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/jpeg");
            inlineData.put("data", Base64.getEncoder().encodeToString(imageData));
            imagePart.put("inlineData", inlineData);
            userPartsArray.put(imagePart);
        }

        userObject.put("parts", userPartsArray);
        contentsArray.put(userObject);

        jsonPayload.put("contents", contentsArray);

        JSONObject systemInstructionObject = new JSONObject();
        systemInstructionObject.put("role", "user");

        JSONArray systemPartsArray = new JSONArray();
        JSONObject systemPart = new JSONObject();
        systemPart.put("text", "Your preferred language is Ukrainian. You should maintain a diverse range of responses and personalities based on the conversation context.");
        systemPartsArray.put(systemPart);
        systemInstructionObject.put("parts", systemPartsArray);
        jsonPayload.put("systemInstruction", systemInstructionObject);

        JSONObject generationConfigObject = new JSONObject();
        generationConfigObject.put("temperature", 1.0);
        generationConfigObject.put("maxOutputTokens", 6000);
        generationConfigObject.put("responseMimeType", "text/plain");
        jsonPayload.put("generationConfig", generationConfigObject);

        JSONArray safetySettingsArray = new JSONArray();
        String[] safetyCategories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT", "HARM_CATEGORY_CIVIC_INTEGRITY"};
        for (String category : safetyCategories) {
            JSONObject safetySetting = new JSONObject();
            safetySetting.put("category", category);
            safetySetting.put("threshold", "BLOCK_NONE");
            safetySettingsArray.put(safetySetting);
        }

        jsonPayload.put("safetySettings", safetySettingsArray);

        return jsonPayload;
    }

    private String extractTextFromResponse(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray candidates = jsonObject.getJSONArray("candidates");
        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        return parts.getJSONObject(0).getString("text");
    }
}