package dev.ua.ikeepcalm.queue.database.entities.timetable;

import dev.ua.ikeepcalm.queue.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queue.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.queue.database.entities.timetable.wrappers.DayWrapper;
import dev.ua.ikeepcalm.queue.database.entities.timetable.wrappers.TimetableWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "timetables")
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
    private ReverenceChat chat;

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
