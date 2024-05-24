package dev.ua.ikeepcalm.lumios.database.entities.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.wrappers.TaskWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity(name = "dueTasks")
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

    public DueTask() {
    }

    public DueTask(TaskWrapper taskWrapper) {
        this.dueDate = taskWrapper.getDueDate();
        this.dueTime = taskWrapper.getDueTime();
        this.taskName = taskWrapper.getTaskName();
        this.url = taskWrapper.getUrl();
    }
}