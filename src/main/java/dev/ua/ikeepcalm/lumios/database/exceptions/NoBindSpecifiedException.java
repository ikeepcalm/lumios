package dev.ua.ikeepcalm.lumios.database.exceptions;

import lombok.experimental.StandardException;

@StandardException
public class NoBindSpecifiedException extends Exception {

    public NoBindSpecifiedException(String message) {
        super(message);
    }
}
