package dev.ua.ikeepcalm.queueupnow.database.entities.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.wrappers.ClassWrapper;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.wrappers.DayWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "day_entries")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private DayOfWeek dayName;

    @ManyToOne
    @JoinColumn
    private TimetableEntry timetableEntry;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ClassEntry> classEntries;

    public DayEntry(DayWrapper dayWrapper) {
        this.dayName = dayWrapper.getDayName();
        this.classEntries = new ArrayList<>();
        for (ClassWrapper classWrapper: dayWrapper.getClassEntries()) {
            classEntries.add(new ClassEntry(classWrapper));
        }
    }

    public DayEntry() {

    }

}