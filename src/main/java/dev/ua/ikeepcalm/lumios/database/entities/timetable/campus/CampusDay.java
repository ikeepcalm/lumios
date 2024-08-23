package dev.ua.ikeepcalm.lumios.database.entities.timetable.campus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CampusDay {

    private String day;
    private List<CampusClass> pairs;

}
