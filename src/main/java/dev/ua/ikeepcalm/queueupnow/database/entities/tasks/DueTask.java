package dev.ua.ikeepcalm.queueupnow.database.entities.tasks;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity(name = "due_tasks")
public class DueTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private LocalDate dueDate;
    @Column
    private LocalTime dueTime;
    @Column
    private String taskName;
    @Column
    private String url;
    @ManyToOne
    private ReverenceChat chat;

}