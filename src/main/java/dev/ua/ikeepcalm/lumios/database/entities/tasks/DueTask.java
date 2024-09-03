package dev.ua.ikeepcalm.lumios.database.entities.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
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

    @Column(length = 2048)
    private String url;

    @Column
    private long author;

    @Column
    private TaskScope scope;

    @Column
    private TaskState state;

    @Column
    private String description;

    @Column
    private String attachment;

    @ManyToOne
    private LumiosChat chat;

    public DueTask() {
    }

    public DueTask(TaskWrapper taskWrapper) {
        this.dueDate = taskWrapper.getDueDate();
        this.dueTime = taskWrapper.getDueTime();
        this.taskName = taskWrapper.getTaskName();
        this.url = taskWrapper.getUrl();
    }
}