package dev.ua.ikeepcalm.lumios.telegram.ai;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            openAI = SimpleOpenAI.builder().apiKey(apiKey).build();
        }
    }

    private String executeSummary(long chatId, int amountOfMessages) {
        List<MessageRecord> userMessages = recordService.findLastMessagesByChatId(chatId, amountOfMessages);
        StringBuilder messagesToSummarize = new StringBuilder();
        for (MessageRecord message : userMessages) {
            if (message.getText().contains("MEDIA") || message.getText().contains("lumios")) {
                continue;
            }

            String fullName = message.getUser().getFullName() == null ? message.getUser().getUsername() : message.getUser().getFullName();

            messagesToSummarize.append(fullName).append(": ").append(message.getText()).append("\n");
        }

        var chatRequest = ChatRequest.builder().model("gpt-4o-mini").message(ChatMessage.SystemMessage.of("""
                You preferred language is Ukrainian. If use custom text formatting, use Markdown syntax. If meet any symbols recognized as Markdown syntax, but not actually used in formatting, escape them with a backslash (\\).
                """)).message(ChatMessage.UserMessage.of("Summarize and structure the following messages, show only important things in the list by the users:\n" + messagesToSummarize)).temperature(0.7).maxTokens(6000).build();

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