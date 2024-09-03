package dev.ua.ikeepcalm.lumios.database.entities.tasks;

public enum TaskState {

    NOT_COMPLETED,
    WAITING_FOR_NAME,
    WAITING_FOR_DATE,
    WAITING_FOR_DESC,
    WAITING_FOR_URL,
    WAITING_FOR_SCOPE,
    WAITING_FOR_ATTACHMENT,
    STAND_BY;

}
