package dev.ua.ikeepcalm.lumios.telegram.exceptions;

public class TelegramApiFailedException extends Exception {
    private final int errorCode;
    private final String apiResponse;

    public TelegramApiFailedException(String message, int errorCode, String apiResponse) {
        super(message);
        this.errorCode = errorCode;
        this.apiResponse = apiResponse;
    }

    public TelegramApiFailedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
        this.apiResponse = null;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getApiResponse() {
        return apiResponse;
    }
}