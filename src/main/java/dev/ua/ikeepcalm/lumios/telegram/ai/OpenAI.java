package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.AiServiceException;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OpenAI {

    private SimpleOpenAI openAI;
    private final RecordService recordService;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAI(RecordService recordService) {
        this.recordService = recordService;
    }

    public CompletableFuture<String> getChatSummary(LumiosChat chat, int amountOfMessages) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                setupOpenAI();
                return executeSummary(chat, amountOfMessages);
            } catch (Exception e) {
                throw new RuntimeException(new AiServiceException(
                    "Failed to generate chat summary", "OpenAI", "summary", e));
            }
        });
    }

    public CompletableFuture<String> getChatResponse(String message, LumiosChat chat) {
        try {
            setupOpenAI();
            return regularChatResponseHandling(message, chat);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                new AiServiceException("Failed to get chat response", "OpenAI", "chat", e));
        }
    }

    private void setupOpenAI() {
        if (openAI == null) {
            openAI = SimpleOpenAI.builder().apiKey(apiKey).build();
        }
    }

    private String executeSummary(LumiosChat chat, int amountOfMessages) {
        long chatId = chat.getChatId();
        List<MessageRecord> userMessages = recordService.findLastMessagesByChatId(chatId, amountOfMessages);
        userMessages.sort(Comparator.comparing(MessageRecord::getDate));

        StringBuilder messagesToSummarize = new StringBuilder();
        for (MessageRecord message : userMessages) {
            if (message.getText().contains("MEDIA") || message.getText().contains("lumios")) {
                continue;
            }

            String fullName = message.getUser().getFullName() == null ? message.getUser().getUsername() : message.getUser().getFullName();

            messagesToSummarize.append(fullName).append(": ").append(message.getText()).append("\n");
        }

        boolean isEn = (chat != null && "en".equals(chat.getLanguage()));
        boolean isZh = (chat != null && "zh".equals(chat.getLanguage()));
        String promptInstruction;
        if (isEn) {
            promptInstruction =
                """
                As a professional summarizer, create a concise and comprehensive summary of the provided conversation in group chat, while adhering to these guidelines:
                    1. Craft a summary that is detailed, thorough, in-depth, and complex, while maintaining clarity and conciseness.
                    2. Incorporate main ideas and essential information, eliminating extraneous language and focusing on critical aspects.
                    3. Rely strictly on the provided text, without including external information.
                    4. Format the summary in paragraph form for easy understanding.
                    5. Summary should be divided into paragraphs, each covering a different aspect of the conversation including names or tags of the participants.
                By following this optimized prompt, you will generate an effective summary that encapsulates the essence of the given text in a clear, concise, and reader-friendly manner.
                """;
        } else if (isZh) {
            promptInstruction =
                """
                作为一名专业的总结者，请为提供的群聊对话创建一个简明且全面的总结，并遵循以下指南：
                    1. 制作一个详尽、深入且复杂的总结，同时保持清晰和简练。
                    2. 整合主要观点和关键信息，去除无关冗余的话，专注于核心内容。
                    3. 严格依赖提供的文本，不要包含外部信息。
                    4. 采用段落形式排版，以便于理解。
                    5. 总结应分为若干段落，每段涵盖对话的不同方面，并包括参与者的姓名或标签。
                遵循此优化提示，您将生成一份有效的总结，以清晰、简明且易于阅读的方式概括给定文本的实质。
                """;
        } else {
            promptInstruction =
                """
                Як професійний сумаризатор, створіть стислий та вичерпний підсумок наданої розмови у груповому чаті, дотримуючись наступних вказівок:
                    1. Створіть підсумок, який є деталізованим, ретельним, глибоким та комплексним, водночас зберігаючи ясність та лаконічність.
                    2. Включайте головні ідеї та важливу інформацію, відкидаючи зайві слова та зосереджуючись на критичних аспектах.
                    3. Покладайтеся виключно на наданий текст розмови, без залучення зовнішньої інформації.
                    4. Сформатуйте підсумок у вигляді абзаців для легкого сприйняття.
                    5. Підсумок має бути розділений на абзаци, кожен з яких охоплює окремий аспект розмови, включаючи імена або теги учасників.
                Дотримуючись цього оптимізованого запиту, ви створите ефективний підсумок, який чітко, лаконічно та зручно для читача передає суть наданого тексту.
                """;
        }

        String prompt = promptInstruction + ":\n" + messagesToSummarize;

        String preferredLanguage = "Ukrainian";
        if (isEn) {
            preferredLanguage = "English";
        } else if (isZh) {
            preferredLanguage = "Chinese";
        }

        String systemPrompt = """
                Your preferred language is %s.

                CRITICAL - Formatting Rules:
                - DO NOT manually escape characters (no backslashes before special chars)
                - The system will handle all escaping automatically
                - Use `backticks` for: code, file names, technical terms, variables, emails
                - Use ```language\\ncode\\n``` for code blocks
                - Write naturally - just use backticks for technical content

                Examples:
                - ✓ Use `my_variable` for variables
                - ✓ Use `user@example.com` for emails
                - ✗ Do NOT write my_variable without backticks
                - ✗ Do NOT add backslashes yourself
                """.formatted(preferredLanguage);

        var chatRequest = ChatRequest.builder().model("gpt-4o")
                .message(ChatMessage.SystemMessage.of(systemPrompt))
                .message(ChatMessage.UserMessage.of(prompt + ":\n" + messagesToSummarize)).temperature(0.4).maxTokens(8000).build();

        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        return chatResponse.firstContent();
    }

    private CompletableFuture<String> regularChatResponseHandling(String message, LumiosChat chat) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean isEn = (chat != null && "en".equals(chat.getLanguage()));
                boolean isZh = (chat != null && "zh".equals(chat.getLanguage()));
                String preferredLang = "Ukrainian";
                if (isEn) {
                    preferredLang = "English";
                } else if (isZh) {
                    preferredLang = "Chinese";
                }
                String systemPrompt = """
                        Act as if you are talking to intelligent interlocutors who understand your technical programming concepts perfectly, but still respond briefly and concisely.
                        Your preferred language is %s.
                        If asked about programming concepts, you can provide detailed explanations and examples, preferably in Java.
                        """.formatted(preferredLang) + """

                        CRITICAL - Formatting Rules (DO NOT ESCAPE MANUALLY):
                        1. DO NOT add backslashes before special characters - the system handles escaping
                        2. Use `backticks` for: variables, file names, paths, technical terms, emails, URLs
                        3. Use ```language\\ncode\\n``` for multi-line code blocks
                        4. Write naturally and just wrap technical terms in backticks
                        5. Examples:
                        - ✓ Variable `my_variable`
                        - ✗ Variable my_variable
                        - ✓ Email `user@example.com`
                        - ✓ File `Main.java`
                        6. Always close code blocks and formatting tags
                        7. When in doubt, use backticks for safety
                        """;

                var chatRequest = ChatRequest.builder()
                    .model("gpt-4o-mini")
                    .message(ChatMessage.SystemMessage.of(systemPrompt))
                    .message(ChatMessage.UserMessage.of(message))
                    .temperature(0.0)
                    .maxTokens(3000)
                    .build();
                
                var futureChat = openAI.chatCompletions().create(chatRequest);
                var chatResponse = futureChat.join();
                return chatResponse.firstContent();
            } catch (Exception e) {
                throw new RuntimeException(new AiServiceException(
                    "Failed to process chat request", "OpenAI", "chat", e));
            }
        });
    }
}