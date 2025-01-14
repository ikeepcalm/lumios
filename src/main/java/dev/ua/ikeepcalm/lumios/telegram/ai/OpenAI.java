package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OpenAI {

    private SimpleOpenAI openAI;
    private final RecordService recordService;
    private final Environment environment;

    public OpenAI(RecordService recordService, Environment environment) {
        this.recordService = recordService;
        this.environment = environment;
    }

    public CompletableFuture<String> getChatSummary(long chatId, int amountOfMessages) {
        setupOpenAI();

        return CompletableFuture.completedFuture(executeSummary(chatId, amountOfMessages));
    }

    public CompletableFuture<String> getChatResponse(String message, long chatId) {
        setupOpenAI();

        return regularChatResponseHandling(message, chatId);
    }

    private void setupOpenAI() {
        if (openAI == null) {
            openAI = SimpleOpenAI.builder().apiKey(environment.getProperty("OPENAI_API_KEY")).build();
        }
    }

    private String executeSummary(long chatId, int amountOfMessages) {
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

        String prompt = """
                As a professional summarizer, create a concise and comprehensive summary of the provided conversation in group chat, while adhering to these guidelines:
                    1. Craft a summary that is detailed, thorough, in-depth, and complex, while maintaining clarity and conciseness.
                    2. Incorporate main ideas and essential information, eliminating extraneous language and focusing on critical aspects.
                    3. Rely strictly on the provided text, without including external information.
                    4. Format the summary in paragraph form for easy understanding.
                    5. Summary should be divided into paragraphs, each covering a different aspect of the conversation including names or tags of the participants.
                By following this optimized prompt, you will generate an effective summary that encapsulates the essence of the given text in a clear, concise, and reader-friendly manner.
                
                """;

        var chatRequest = ChatRequest.builder().model("gpt-4o")
                .message(ChatMessage.SystemMessage.of("You preferred language is Ukrainian. If use custom text formatting, use Markdown syntax. If meet any symbols recognized as Markdown syntax, but not actually used in formatting, escape them with a backslash (\\)."))
                .message(ChatMessage.UserMessage.of(prompt + ":\n" + messagesToSummarize)).temperature(0.4).maxTokens(8000).build();

        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        return chatResponse.firstContent();
    }

    private CompletableFuture<String> regularChatResponseHandling(String message, long chatId) {

        var chatRequest = ChatRequest.builder().model("gpt-4o-mini").message(ChatMessage.SystemMessage.of("""
                Act as if you are talking to intelligent interlocutors who understand your technical programming concepts perfectly, but still respond briefly and concisely. Your preferred language is Ukrainian.
                If asked about programming concepts, you can provide detailed explanations and examples, preferably in Java. If use custom text formatting, use Markdown syntax.
                """)).message(ChatMessage.UserMessage.of(message)).temperature(0.0).maxTokens(3000).build();
        var futureChat = openAI.chatCompletions().create(chatRequest);
        var chatResponse = futureChat.join();
        return CompletableFuture.completedFuture(chatResponse.firstContent());
    }
}