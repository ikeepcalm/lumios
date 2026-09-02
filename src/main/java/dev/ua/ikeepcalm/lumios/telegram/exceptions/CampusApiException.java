package dev.ua.ikeepcalm.lumios.telegram.exceptions;

import lombok.Getter;

/**
 * Thrown when the KPI Campus API cannot be reached or returns something we cannot parse.
 * Unchecked on purpose so that {@code ImportUtil} stays usable as a plain helper, but
 * distinct enough for interaction handlers to turn it into a user-facing message.
 */
@Getter
public class CampusApiException extends RuntimeException {

    private final String operation;

    public CampusApiException(String message, String operation) {
        super(message);
        this.operation = operation;
    }

    public CampusApiException(String message, String operation, Throwable cause) {
        super(message, cause);
        this.operation = operation;
    }

}
