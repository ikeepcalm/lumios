package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
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
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class Gemini {

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKey;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
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
        return getChatResponse(inputText, chatId, null, null, null, null);
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId, byte[] imageData) {
        return getChatResponse(inputText, chatId, imageData, null, null, null);
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId, byte[] imageData, LumiosUser user, LumiosChat chat) {
        return getChatResponse(inputText, chatId, imageData, null, user, chat);
    }

    public CompletableFuture<String> getChatResponseForReply(String inputText, Long chatId, Long replyToMessageId) {
        return getChatResponse(inputText, chatId, null, replyToMessageId, null, null);
    }

    public CompletableFuture<String> getChatResponseForReply(String inputText, Long chatId, Long replyToMessageId, LumiosUser user, LumiosChat chat) {
        return getChatResponse(inputText, chatId, null, replyToMessageId, user, chat);
    }

    public CompletableFuture<String> getChatSummary(long chatId, int amountOfMessages) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeSummary(chatId, amountOfMessages);
            } catch (Exception e) {
                log.error("Failed to generate chat summary with Gemini", e);
                throw new RuntimeException("Failed to generate chat summary", e);
            }
        }, executorService);
    }

    public CompletableFuture<String> getChatResponse(String inputText, Long chatId, byte[] imageData, Long replyToMessageId, LumiosUser user, LumiosChat chat) {
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
                        JSONObject jsonPayload = getJsonObject(inputText, chatId, finalImageKey, replyToMessageId, user, chat);
                        log.debug("Payload size: {} bytes", jsonPayload.toString().length());

                        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + key);
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

                        return extractTextFromResponse(response.toString());

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
            }
        }, executorService);
    }


    @NotNull
    private JSONObject getJsonObject(String inputText, Long chatId, String imageKey, Long replyToMessageId, LumiosUser user, LumiosChat chat) {
        JSONArray conversationContext;
        if (replyToMessageId != null) {
            conversationContext = conversationService.getReplyChainContext(chatId, replyToMessageId);
        } else {
            conversationContext = conversationService.getEnhancedConversationContext(chatId);
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

        // Build enhanced system prompt with context
        String systemPrompt = buildEnhancedSystemPrompt(user, chat);
        systemPart.put("text", systemPrompt);
        systemParts.put(systemPart);
        systemInstruction.put("parts", systemParts);
        jsonPayload.put("systemInstruction", systemInstruction);

        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.8);
        genConfig.put("maxOutputTokens", 900);
        genConfig.put("topP", 0.95);
        genConfig.put("topK", 64);

        // Add thinking mode for better reasoning on complex queries
        JSONObject thinkingConfig = new JSONObject();
        thinkingConfig.put("thinkingBudget", 1024); // Use 1024 tokens for reasoning
        genConfig.put("thinkingConfig", thinkingConfig);

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

    private String executeSummary(long chatId, int amountOfMessages) throws Exception {
        List<MessageRecord> userMessages = recordService.findLastMessagesByChatId(chatId, amountOfMessages);
        userMessages.sort(Comparator.comparing(MessageRecord::getDate));

        StringBuilder messagesToSummarize = new StringBuilder();
        for (MessageRecord message : userMessages) {
            if ( message.getText().contains("MEDIA") || message.getText().contains("lumios")) {
                continue;
            }

            if (message.getUser() == null) {
                continue;
            }

            String fullName = message.getUser().getFullName() == null ? message.getUser().getUsername() : message.getUser().getFullName();
            messagesToSummarize.append(fullName).append(": ").append(message.getText()).append("\n");
        }

        String prompt = """
                As a professional summarizer, create a concise and comprehensive summary of the provided conversation in group chat, while adhering to these guidelines:
                    1. Craft a summary that is detailed, thorough, in-depth, and complex, while maintaining clarity and conciseness.
                    2. Incorporate main ideas and essential information, eliminating extraneous language and focusing on critical aspects.
                    3. Rely strictly on the provided text, without including external information.
                    4. Format the summary in paragraph form for easy understanding.
                    5. Summary should be divided into paragraphs, each covering a different aspect of the conversation including names or tags of the participants.
                By following this optimized prompt, you will generate an effective summary that encapsulates the essence of the given text in a clear, concise, and reader-friendly manner.
                
                """ + ":\n" + messagesToSummarize;

        JSONObject jsonPayload = createSummaryPayload(prompt);

        for (String key : apiKey) {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + key);
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

                return extractTextFromResponse(response.toString());
            } catch (Exception e) {
                log.error("Failed to get summary with key: {}", key.substring(0, 8) + "...", e);
                if (key.equals(apiKey.get(apiKey.size() - 1))) {
                    throw e;
                }
            }
        }
        throw new RuntimeException("All API keys failed for summary generation");
    }

    private JSONObject createSummaryPayload(String prompt) {
        JSONObject jsonPayload = new JSONObject();
        JSONArray contentsArray = new JSONArray();

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONArray parts = new JSONArray();

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);

        userMessage.put("parts", parts);
        contentsArray.put(userMessage);
        jsonPayload.put("contents", contentsArray);

        JSONObject systemInstruction = new JSONObject();
        systemInstruction.put("role", "user");
        JSONArray systemParts = new JSONArray();
        JSONObject systemPart = new JSONObject();
        systemPart.put("text", "You preferred language is Ukrainian. If use custom text formatting, use Markdown syntax. If meet any symbols recognized as Markdown syntax, but not actually used in formatting, escape them with a backslash (\\).");
        systemParts.put(systemPart);
        systemInstruction.put("parts", systemParts);
        jsonPayload.put("systemInstruction", systemInstruction);

        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.4);
        genConfig.put("maxOutputTokens", 8000);
        genConfig.put("topP", 0.9);
        genConfig.put("topK", 40);
        jsonPayload.put("generationConfig", genConfig);

        return jsonPayload;
    }

    /**
     * Builds an enhanced system prompt with context about the user and chat
     */
    private String buildEnhancedSystemPrompt(LumiosUser user, LumiosChat chat) {
        StringBuilder prompt = new StringBuilder();

        // Current Date/Time
        String currentDateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        prompt.append("Current Date and Time: ").append(currentDateTime).append("\n\n");

        // Role and Identity
        prompt.append("# Role and Identity\n\n");
        prompt.append("Your responses must be **extremely concise, direct, and code-focused**.\n");
        prompt.append("Do not waste tokens on pleasantries, conversational filler, or introductions unless explicitly asked.\n\n");

        // Current context
        prompt.append("# Current Context\n\n");
        if (chat != null) {
            prompt.append("**Chat Environment:**\n");
            prompt.append("- Chat name: ").append(chat.getName() != null ? chat.getName() : "Unnamed chat").append("\n");
            if (chat.getDescription() != null && !chat.getDescription().isEmpty()) {
                prompt.append("- Description: ").append(chat.getDescription()).append("\n");
            }
            prompt.append("\n");
        }

        if (user != null) {
            prompt.append("**Current User:**\n");
            String displayName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            prompt.append("- Name: ").append(displayName).append("\n");
            prompt.append("- Activity level (reverence): ").append(user.getReverence());
            prompt.append(" (higher values indicate more active participation)\n");
            if (user.getCredits() > 0) {
                prompt.append("- Available credits: ").append(user.getCredits()).append("\n");
            }
            prompt.append("\n");
        }

        // Output formatting
        prompt.append("# Output Formatting & Style (CRITICAL)\n\n");
        prompt.append("1. **Telegram Legacy Markdown:** You are communicating via a Telegram Bot using strict Legacy Markdown.\n");
        prompt.append("   - **Bold:** `*text*`\n");
        prompt.append("   - **Italic:** `_text_` (Avoid this if possible, it's prone to errors with underscores)\n");
        prompt.append("   - **Inline Code:** ` `text` ` (Use this for ALL technical terms, variables, paths, etc.)\n");
        prompt.append("   - **Code Blocks:** ```language\\ncode\\n```\n");
        prompt.append("2. **Underscore Handling:** You MUST wrap any word containing an underscore in backticks. Example: `my_variable`, NOT my_variable. This is non-negotiable.\n");
        prompt.append("3. **Conciseness:** \n");
        prompt.append("   - If asked for code, provide ONLY the code and a minimal explanation.\n");
        prompt.append("   - Do not say 'Here is the code' or 'I hope this helps'.\n");
        prompt.append("   - Go straight to the point.\n");
        prompt.append("4. **Language:** Ukrainian (unless asked in English).\n\n");

        prompt.append("Your goal is to be a highly efficient, error-free coding companion. 💻");

        return prompt.toString();
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