package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class Gemini {

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKey;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final GeminiConversationService conversationService;
    private final RecordService recordService;
    private final TimetableService timetableService;

    private static final int MAX_CACHE_ENTRIES = 10;
    private static final int MAX_IMAGE_SIZE = 1024 * 1024; // 1MB limit
    private final ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();

    // Models to try in order (fallback on 429 rate limit errors)
    private static final String[] GEMINI_MODELS = {
        "gemini-2.5-flash-lite",
        "gemini-2.5-flash",
        "gemini-3-flash",
        "gemma-3-12b",
        "gemma-3-27b"
    };

    public Gemini(GeminiConversationService conversationService, RecordService recordService, TimetableService timetableService) {
        this.conversationService = conversationService;
        this.recordService = recordService;
        this.timetableService = timetableService;
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
                Exception lastException = null;

                // Try each model in order
                for (String model : GEMINI_MODELS) {
                    for (String key : apiKey) {
                        try {
                            JSONObject jsonPayload = getJsonObject(inputText, chatId, finalImageKey, replyToMessageId, user, chat, model);
                            log.debug("Trying model {} with payload size: {} bytes", model, jsonPayload.toString().length());

                            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + key);
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

                            int responseCode = connection.getResponseCode();

                            // Check for rate limit error
                            if (responseCode == 429) {
                                log.warn("Rate limit (429) reached for model {} with key {}, trying next option", model, key.substring(0, Math.min(8, key.length())));
                                lastException = new RuntimeException("Rate limit reached for model " + model);
                                continue; // Try next key for this model
                            }

                            // Check for other HTTP errors
                            if (responseCode >= 400) {
                                log.error("HTTP error {} for model {} with key {}", responseCode, model, key.substring(0, Math.min(8, key.length())));
                                lastException = new RuntimeException("HTTP error " + responseCode + " for model " + model);
                                continue; // Try next key
                            }

                            // Success - read response
                            StringBuilder response = new StringBuilder();
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                String inputLine;
                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                            }

                            log.info("Successfully got response from model {} with key {}", model, key.substring(0, Math.min(8, key.length())));
                            return extractTextFromResponse(response.toString());

                        } catch (Exception e) {
                            log.error("Error with model {} and key {}: {}", model, key.substring(0, Math.min(8, key.length())), e.getMessage());
                            lastException = e;
                        }
                    }
                    // All keys failed for this model, try next model
                    log.warn("All API keys failed for model {}, trying next model", model);
                }

                // All models and keys failed
                throw new RuntimeException("Failed to get response from Gemini with all models and API keys", lastException);
            } finally {
                if (finalImageKey != null) {
                    imageCache.remove(finalImageKey);
                    log.info("Removed image with key {}", finalImageKey);
                }
            }
        }, executorService);
    }


    @NotNull
    private JSONObject getJsonObject(String inputText, Long chatId, String imageKey, Long replyToMessageId, LumiosUser user, LumiosChat chat, String modelName) {
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

        // Build enhanced system prompt with context including current model
        String systemPrompt = buildEnhancedSystemPrompt(user, chat, modelName);
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
        Exception lastException = null;

        // Try each model in order
        for (String model : GEMINI_MODELS) {
            for (String key : apiKey) {
                try {
                    log.debug("Trying model {} for summary generation", model);

                    URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + key);
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

                    int responseCode = connection.getResponseCode();

                    // Check for rate limit error
                    if (responseCode == 429) {
                        log.warn("Rate limit (429) for summary with model {}, trying next option", model);
                        lastException = new RuntimeException("Rate limit reached for model " + model);
                        continue;
                    }

                    // Check for other HTTP errors
                    if (responseCode >= 400) {
                        log.error("HTTP error {} for summary with model {}", responseCode, model);
                        lastException = new RuntimeException("HTTP error " + responseCode);
                        continue;
                    }

                    StringBuilder response = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }

                    log.info("Successfully generated summary with model {}", model);
                    return extractTextFromResponse(response.toString());

                } catch (Exception e) {
                    log.error("Failed to get summary with model {} and key: {}", model, key.substring(0, Math.min(8, key.length())) + "...", e);
                    lastException = e;
                }
            }
            log.warn("All API keys failed for summary with model {}, trying next model", model);
        }

        throw new RuntimeException("All models and API keys failed for summary generation", lastException);
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
    private String buildEnhancedSystemPrompt(LumiosUser user, LumiosChat chat, String modelName) {
        StringBuilder prompt = new StringBuilder();

        // Current Date/Time and Week
        LocalDate currentDate = LocalDate.now(ZoneId.of("Europe/Kiev"));
        LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
        String currentDateTime = currentDate + " " + currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String dayName = currentDayOfWeek.getDisplayName(TextStyle.FULL, new Locale("uk", "UA"));

        prompt.append("=== CURRENT DATE & TIME ===\n");
        prompt.append(currentDateTime).append(" (").append(dayName).append(")\n");
        prompt.append("Academic week: ").append(WeekValidator.determineWeekDay()).append("\n\n");

        // Current AI Model
        prompt.append("=== YOUR CURRENT MODEL ===\n");
        prompt.append("You are currently running on: ").append(modelName).append("\n");
        prompt.append("If asked which model you are, respond with this exact model name.\n\n");

        // Role and Identity
        prompt.append("=== YOUR ROLE ===\n");
        prompt.append("You are a helpful AI assistant in a Telegram group chat.\n");
        prompt.append("Be concise, direct, and helpful. Avoid unnecessary pleasantries.\n\n");

        // Current context
        if (chat != null) {
            prompt.append("=== CHAT ENVIRONMENT ===\n");
            prompt.append("Chat: ").append(chat.getName() != null ? chat.getName() : "Unnamed chat").append("\n");
            if (chat.getDescription() != null && !chat.getDescription().isEmpty()) {
                prompt.append("Description: ").append(chat.getDescription()).append("\n");
            }
            if (chat.getBotNickname() != null && !chat.getBotNickname().isEmpty()) {
                prompt.append("Your nickname in this chat: ").append(chat.getBotNickname()).append("\n");
            }
            prompt.append("\n");

            // Add timetable context if available
            String timetableContext = getTimetableContext(chat);
            if (!timetableContext.isEmpty()) {
                prompt.append(timetableContext);
            }
        }

        if (user != null) {
            prompt.append("=== CURRENT USER ===\n");
            String displayName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            prompt.append("Name: ").append(displayName).append("\n");
            prompt.append("Activity level: ").append(user.getReverence());
            prompt.append(" (higher = more active in chat)\n");
            if (user.getCredits() > 0) {
                prompt.append("Credits available: ").append(user.getCredits()).append("\n");
            }
            prompt.append("\n");
        }

        // Critical formatting rules
        prompt.append("=== OUTPUT FORMATTING (CRITICAL) ===\n\n");
        prompt.append("You MUST use Telegram MarkdownV2 format. Follow these rules EXACTLY:\n\n");

        prompt.append("1. CODE FORMATTING:\n");
        prompt.append("   - Inline code: `code here`\n");
        prompt.append("   - Code blocks: ```language\\ncode\\n```\n");
        prompt.append("   - ALWAYS use backticks for: variables, file paths, technical terms, URLs\n");
        prompt.append("   - Example: Use `my_variable` not my_variable\n\n");

        prompt.append("2. TEXT FORMATTING:\n");
        prompt.append("   - Bold: *text* or **text**\n");
        prompt.append("   - Italic: _text_ or __text__\n");
        prompt.append("   - IMPORTANT: Underscores in normal text will break formatting!\n");
        prompt.append("   - If text contains _, [ ] ( ) or other special chars, wrap it in `code`\n\n");

        prompt.append("3. SPECIAL CHARACTERS:\n");
        prompt.append("   - These chars have special meaning: _ * [ ] ( ) ~ ` > # + - = | { } . !\n");
        prompt.append("   - If you need to show them literally, wrap in backticks\n");
        prompt.append("   - Example: Email should be: `user@example.com`\n\n");

        prompt.append("4. SAFE PRACTICES:\n");
        prompt.append("   - Use code blocks for multi-line code\n");
        prompt.append("   - Use inline code for file names, paths, commands\n");
        prompt.append("   - Keep formatting simple - prefer code blocks over complex markdown\n");
        prompt.append("   - When in doubt, use backticks\n\n");

        prompt.append("5. LANGUAGE:\n");
        prompt.append("   - Respond in Ukrainian unless the user writes in English\n");
        prompt.append("   - Be natural and conversational\n\n");

        prompt.append("Remember: Your output will be processed by Telegram's MarkdownV2 parser. ");
        prompt.append("Incorrect formatting will cause errors. When uncertain, use backticks.\n");

        return prompt.toString();
    }

    /**
     * Gets timetable context for the current day and upcoming classes
     */
    private String getTimetableContext(LumiosChat chat) {
        if (chat == null || !chat.isTimetableEnabled()) {
            return "";
        }

        try {
            StringBuilder context = new StringBuilder();
            context.append("=== SCHEDULE & CLASSES ===\n");

            // Get current week's timetable
            TimetableEntry timetable = timetableService.findByChatIdAndWeekType(
                chat.getChatId(),
                WeekValidator.determineWeekDay()
            );

            LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Kiev"));
            DayOfWeek today = LocalDate.now(ZoneId.of("Europe/Kiev")).getDayOfWeek();

            // Find today's classes
            List<ClassEntry> todaysClasses = new ArrayList<>();
            for (DayEntry day : timetable.getDays()) {
                if (day.getDayName().equals(today)) {
                    todaysClasses.addAll(day.getClassEntries());
                    break;
                }
            }

            if (todaysClasses.isEmpty()) {
                context.append("No classes scheduled for today.\n\n");
                return context.toString();
            }

            // Sort by time
            todaysClasses.sort(Comparator.comparing(ClassEntry::getStartTime));

            // Find current class
            ClassEntry currentClass = null;
            ClassEntry nextClass = null;

            for (ClassEntry classEntry : todaysClasses) {
                if (currentTime.isAfter(classEntry.getStartTime()) &&
                    currentTime.isBefore(classEntry.getEndTime())) {
                    currentClass = classEntry;
                } else if (currentTime.isBefore(classEntry.getStartTime())) {
                    if (nextClass == null) {
                        nextClass = classEntry;
                    }
                }
            }

            if (currentClass != null) {
                context.append("CURRENT CLASS (").append(currentClass.getStartTime())
                       .append("-").append(currentClass.getEndTime()).append("):\n");
                context.append("  ").append(currentClass.getName())
                       .append(" (").append(currentClass.getClassType()).append(")\n");
            }

            if (nextClass != null) {
                context.append("NEXT CLASS (").append(nextClass.getStartTime())
                       .append("-").append(nextClass.getEndTime()).append("):\n");
                context.append("  ").append(nextClass.getName())
                       .append(" (").append(nextClass.getClassType()).append(")\n");
            }

            // Add all today's classes summary
            context.append("\nAll classes today:\n");
            for (ClassEntry classEntry : todaysClasses) {
                context.append("  ").append(classEntry.getStartTime())
                       .append("-").append(classEntry.getEndTime())
                       .append(" ").append(classEntry.getName())
                       .append(" (").append(classEntry.getClassType()).append(")\n");
            }

            context.append("\n");
            return context.toString();

        } catch (Exception e) {
            log.debug("Could not fetch timetable context: {}", e.getMessage());
            return "";
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