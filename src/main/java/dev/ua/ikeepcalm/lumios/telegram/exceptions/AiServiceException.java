package dev.ua.ikeepcalm.lumios.telegram.exceptions;

public class AiServiceException extends Exception {
    private final String service;
    private final String operation;

    public AiServiceException(String message, String service, String operation) {
        super(message);
        this.service = service;
        this.operation = operation;
    }

    public AiServiceException(String message, String service, String operation, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.operation = operation;
    }

    public String getService() {
        return service;
    }

    public String getOperation() {
        return operation;
    }
}