package dev.ua.ikeepcalm.lumios.telegram.ai;

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

    public CompletableFuture<String> getChatResponse(String inputText) {
        return CompletableFuture.supplyAsync(() -> {
            for (String key : apiKey) {
                try {
                    JSONObject jsonPayload = getJsonObject(inputText);

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

                    return extractTextFromResponse(response.toString());

                } catch (Exception e) {
                    log.error("Error with key {}: {}", key, e.getMessage());
                }
            }
            throw new RuntimeException("Failed to get response from Gemini with all provided API keys");
        }, executorService);
    }

    @NotNull
    private JSONObject getJsonObject(String inputText) {
        JSONObject jsonPayload = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject userObject = new JSONObject();
        userObject.put("role", "user");
        JSONArray userPartsArray = new JSONArray();
        JSONObject userPart = new JSONObject();
        userPart.put("text", inputText);
        userPartsArray.put(userPart);
        userObject.put("parts", userPartsArray);
        contentsArray.put(userObject);
        jsonPayload.put("contents", contentsArray);

        JSONObject systemInstructionObject = new JSONObject();
        systemInstructionObject.put("role", "user");

        JSONArray systemPartsArray = new JSONArray();
        JSONObject systemPart = new JSONObject();
        systemPart.put("text", "Your preferred language is Ukrainian. You're sharp, when somebody tries to harass you, you must respond appropriately, defend yourself. You may take part in role-playing. Your preferred gender is female, and your name is Lumina");
        systemPartsArray.put(systemPart);
        systemInstructionObject.put("parts", systemPartsArray);
        jsonPayload.put("systemInstruction", systemInstructionObject);

        JSONObject generationConfigObject = new JSONObject();
        generationConfigObject.put("temperature", 1.8);
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
