package dev.ua.ikeepcalm.lumios.database.entities.timetable.campus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CampusTimetable {

    private String groupCode;
    private List<CampusDay> scheduleFirstWeek;
    private List<CampusDay> scheduleSecondWeek;

}