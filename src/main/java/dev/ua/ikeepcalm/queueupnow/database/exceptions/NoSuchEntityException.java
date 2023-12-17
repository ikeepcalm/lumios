package dev.ua.ikeepcalm.queue.database.exceptions;

import lombok.experimental.StandardException;

@StandardException
public class NoSuchEntityException extends Exception{

    public NoSuchEntityException(String message) {
        super(message);
    }
}
