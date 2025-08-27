package dev.ua.ikeepcalm.lumios.telegram.services;

import dev.ua.ikeepcalm.lumios.telegram.exceptions.AiServiceException;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.MessageProcessingException;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.TelegramApiFailedException;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandlingService {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingService.class);
    
    public String handleTelegramApiError(TelegramApiFailedException e, String operation) {
        log.error("Telegram API error during {}: Code {}, Message: {}", 
            operation, e.getErrorCode(), e.getApiResponse(), e);
        
        return MessageFormatter.formatApiErrorMessage(e.getErrorCode(), operation);
    }
    
    public String handleMessageProcessingError(MessageProcessingException e) {
        log.error("Message processing error for chat {} ({}): {}", 
            e.getChatId(), e.getMessageType(), e.getMessage(), e);
            
        return MessageFormatter.formatErrorMessage(
            "Помилка обробки повідомлення типу " + e.getMessageType()
        );
    }
    
    public String handleAiServiceError(AiServiceException e) {
        log.error("AI service error in {} during {}: {}", 
            e.getService(), e.getOperation(), e.getMessage(), e);
            
        return MessageFormatter.formatErrorMessage(
            "Сервіс " + e.getService() + " тимчасово недоступний. Спробуйте пізніше."
        );
    }
    
    public String handleGenericError(Exception e, String context) {
        log.error("Unexpected error in {}: {}", context, e.getMessage(), e);
        
        return MessageFormatter.formatErrorMessage(
            "Виникла несподівана помилка. Спробуйте ще раз."
        );
    }
    
    public boolean isRecoverableError(TelegramApiFailedException e) {
        return switch (e.getErrorCode()) {
            case 429, 500, 502, 503 -> true; // Rate limit, server errors
            case 400 -> false; // Bad request - usually not recoverable
            case 403 -> false; // Forbidden - bot blocked
            case 404 -> false; // Not found
            default -> false;
        };
    }
    
    public int getRetryDelay(TelegramApiFailedException e, int attempt) {
        return switch (e.getErrorCode()) {
            case 429 -> Math.min(1000 * attempt, 10000); // Rate limit exponential backoff
            case 500, 502, 503 -> 2000 * attempt; // Server error linear backoff
            default -> 0;
        };
    }
}