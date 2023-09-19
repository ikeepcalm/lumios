package dev.ua.ikeepcalm.merged.entities.tasks;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
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