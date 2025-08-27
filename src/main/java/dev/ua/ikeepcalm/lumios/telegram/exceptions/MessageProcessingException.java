package dev.ua.ikeepcalm.lumios.telegram.exceptions;

public class MessageProcessingException extends Exception {
    private final Long chatId;
    private final String messageType;

    public MessageProcessingException(String message, Long chatId, String messageType) {
        super(message);
        this.chatId = chatId;
        this.messageType = messageType;
    }

    public MessageProcessingException(String message, Long chatId, String messageType, Throwable cause) {
        super(message, cause);
        this.chatId = chatId;
        this.messageType = messageType;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getMessageType() {
        return messageType;
    }
}