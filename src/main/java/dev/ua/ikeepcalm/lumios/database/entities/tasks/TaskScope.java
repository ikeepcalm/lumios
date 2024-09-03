package dev.ua.ikeepcalm.lumios.database.entities.tasks;

import lombok.Getter;

@Getter
public enum TaskScope {

    SINGLE("Тільки для мене"),
    EVERYONE("Для всіх у чаті"),
    NOT_ME("Для всіх, окрім мене");

    private final String name;

    TaskScope(String name) {
        this.name = name;
    }

}
