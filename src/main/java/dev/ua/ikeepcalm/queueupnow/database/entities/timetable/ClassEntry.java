package dev.ua.ikeepcalm.queueupnow.database.entities.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.wrappers.ClassWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity(name = "class_entries")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String url;

    @Column
    @Enumerated(EnumType.STRING)
    private ClassType classType;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @ManyToOne(cascade = CascadeType.ALL)
    private DayEntry dayEntry;

    public ClassEntry(ClassWrapper classWrapper) {
        this.name = classWrapper.getName();
        this.url = classWrapper.getUrl();
        this.classType = classWrapper.getClassType();
        this.startTime = classWrapper.getStartTime();
        this.endTime = classWrapper.getEndTime();
    }

    public ClassEntry() {

    }
}
