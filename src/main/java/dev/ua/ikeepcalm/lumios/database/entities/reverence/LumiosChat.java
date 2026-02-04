package dev.ua.ikeepcalm.lumios.database.entities.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "chats")
public class LumiosChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long chatId;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private LocalDateTime lastWheelDate;

    @Column(columnDefinition = "boolean default true")
    private boolean isTimetableEnabled;

    @Column(columnDefinition = "boolean default false")
    private boolean isDiceEnabled;

    @Column(columnDefinition = "boolean default false")
    private boolean isAiEnabled;

    @Column(columnDefinition = "boolean default false")
    private boolean isPlainTimetableEnabled;

    @Column
    private AiModel aiModel;

    @Column(columnDefinition = "integer default 2")
    private int summaryLimit;

    @Column(columnDefinition = "integer default 10")
    private int communicationLimit;

    @Column
    private String botNickname;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LumiosUser> users;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DueTask> tasks;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimetableEntry> timetables;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatShot> shots;

}

