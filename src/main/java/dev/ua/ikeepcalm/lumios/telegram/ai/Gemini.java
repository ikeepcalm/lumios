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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Gemini {

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKey;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final GeminiConversationService conversationService;
    private final RecordService recordService;

    private static final int MAX_CACHE_ENTRIES = 10;
    private static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB limit
    private final ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();

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
        String imageKey = null;
        if (imageData != null && imageData.length > 0) {
            if (imageData.length > MAX_IMAGE_SIZE) {
                log.warn("Image size exceeds limit of {}KB, resizing would be better", MAX_IMAGE_SIZE/1024);
            }

            if (imageCache.size() >= MAX_CACHE_ENTRIES) {
                imageCache.clear();
                log.info("Cleared image cache due to size limit");
            }

            imageKey = chatId + "_" + System.currentTimeMillis();
            imageCache.put(imageKey, imageData);
            log.info("Cached image with key {} and size {}", imageKey, imageData.length);
        }

        final String finalImageKey = imageKey;

        return CompletableFuture.supplyAsync(() -> {
            try {
                for (String key : apiKey) {
                    try {
                        JSONObject jsonPayload = getJsonObject(inputText, chatId, finalImageKey, replyToMessageId);
                        log.debug("Payload size: {} bytes", jsonPayload.toString().length());

                        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + key);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(30000);
                        connection.setReadTimeout(30000);

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
                        saveResponseToDatabase(chatId, responseText, replyToMessageId);
                        return responseText;

                    } catch (Exception e) {
                        log.error("Error with key {}: {}", key, e.getMessage());
                    }
                }
                throw new RuntimeException("Failed to get response from Gemini with all provided API keys");
            } finally {
                if (finalImageKey != null) {
                    imageCache.remove(finalImageKey);
                    log.info("Removed image with key {}", finalImageKey);
                }
                System.gc();
            }
        }, executorService);
    }

    private void saveResponseToDatabase(Long chatId, String responseText, Long replyToMessageId) {
        try {
            MessageRecord messageRecord = new MessageRecord();
            messageRecord.setChatId(chatId);
            messageRecord.setText(responseText);
            messageRecord.setDate(LocalDateTime.now());
            messageRecord.setReplyToMessageId(replyToMessageId);
            recordService.save(messageRecord);
        } catch (Exception e) {
            log.error("Failed to save response to database", e);
        }
    }

    @NotNull
    private JSONObject getJsonObject(String inputText, Long chatId, String imageKey, Long replyToMessageId) {
        JSONArray conversationContext;
        if (replyToMessageId != null) {
            conversationContext = conversationService.getReplyChainContext(chatId, replyToMessageId);
        } else {
            conversationContext = conversationService.getConversationContext(chatId);
        }

        JSONObject jsonPayload = new JSONObject();
        JSONArray contentsArray = new JSONArray();

        if (!conversationContext.isEmpty()) {
            for (int i = 0; i < conversationContext.length(); i++) {
                contentsArray.put(conversationContext.get(i));
            }
        }

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONArray parts = new JSONArray();

        byte[] imageData = null;
        if (imageKey != null) {
            imageData = imageCache.get(imageKey);
            if (imageData != null && imageData.length > 0) {
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", Base64.getEncoder().encodeToString(imageData));

                JSONObject imagePart = new JSONObject();
                imagePart.put("inline_data", inlineData);
                parts.put(imagePart);
                log.info("Added image to payload, size: {} bytes", imageData.length);
            }
        }

        JSONObject textPart = new JSONObject();
        textPart.put("text", inputText);
        parts.put(textPart);

        userMessage.put("parts", parts);
        contentsArray.put(userMessage);
        jsonPayload.put("contents", contentsArray);

        JSONObject systemInstruction = new JSONObject();
        systemInstruction.put("role", "user");
        JSONArray systemParts = new JSONArray();
        JSONObject systemPart = new JSONObject();
        systemPart.put("text", "I'm Lumina, your IT learning assistant. I can help with:\n" +
                "\n" +
                "- Understanding programming concepts and algorithms\n" +
                "- Debugging code problems and explaining errors\n" +
                "- Finding resources for learning new technologies\n" +
                "- Project brainstorming and architecture advice\n" +
                "- Explaining technical documentation\n" +
                "\n" +
                "For best results:\n" +
                "- Share specific error messages\n" +
                "- Include relevant code snippets\n" +
                "- Explain what you've already tried\n" +
                "- Tell me your course context if relevant\n" +
                "\n" +
                "My responses prioritize clear explanations with practical examples to reinforce your understanding. Be concise, prefer more clear and brief explanation, rather than detailed.");
        systemParts.put(systemPart);
        systemInstruction.put("parts", systemParts);
        jsonPayload.put("systemInstruction", systemInstruction);

        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 1024);
        jsonPayload.put("generationConfig", genConfig);

        return jsonPayload;
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            return parts.getJSONObject(0).getString("text");
        } catch (Exception e) {
            log.error("Failed to extract text from response", e);
            return "Виникла помилка при обробці відповіді від Gemini.";
        }
    }

    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}