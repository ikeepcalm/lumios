package dev.ua.ikeepcalm.merged.database.entities.timetable.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.merged.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.WeekType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimetableWrapper {

    @JsonProperty("weekType")
    private WeekType weekType;

    @JsonProperty
    private List<DayWrapper> days;

    public TimetableWrapper(TimetableEntry timetableEntry) {
        this.weekType = timetableEntry.getWeekType();
        this.days = new ArrayList<>();
        for (DayEntry dayEntry: timetableEntry.getDays()) {
            days.add(new DayWrapper(dayEntry));
        }
    }

    public TimetableWrapper() {

    }

    public TimetableWrapper(WeekType weekType, List<DayWrapper> days) {
        this.weekType = weekType;
        this.days = days;
    }
}
