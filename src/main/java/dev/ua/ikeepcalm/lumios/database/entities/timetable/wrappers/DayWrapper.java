package dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayWrapper {

    @JsonProperty("dayName")
    private DayOfWeek dayName;

    @JsonProperty("classEntries")
    private List<ClassWrapper> classEntries;

    public DayWrapper(DayEntry dayEntry) {
        this.dayName = dayEntry.getDayName();
        this.classEntries = new ArrayList<>();
        for (ClassEntry classEntry : dayEntry.getClassEntries()) {
            classEntries.add(new ClassWrapper(classEntry));
        }
    }

    public DayWrapper() {

    }

    public DayWrapper(DayOfWeek dayName, List<ClassWrapper> classEntries) {
        this.dayName = dayName;
        this.classEntries = classEntries;
    }
}