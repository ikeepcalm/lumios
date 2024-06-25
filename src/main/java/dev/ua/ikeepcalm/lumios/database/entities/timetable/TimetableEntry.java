package dev.ua.ikeepcalm.lumios.database.entities.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers.DayWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers.TimetableWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "timetableEntries")
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private WeekType weekType;

    @JoinColumn
    @OneToMany(cascade = CascadeType.ALL)
    private List<DayEntry> days;

    @JoinColumn
    @ManyToOne
    private LumiosChat chat;

    public TimetableEntry(TimetableWrapper timetableWrapper) {
        this.weekType = timetableWrapper.getWeekType();
        this.days = new ArrayList<>();
        for (DayWrapper dayWrapper : timetableWrapper.getDays()) {
            days.add(new DayEntry(dayWrapper));
        }
    }

    public TimetableEntry() {

    }
}
