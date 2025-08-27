package dev.ua.ikeepcalm.lumios.database.entities.timetable.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampusClass {

    private String teacherName;
    private String lecturerId;
    private String type;
    private String time;
    private String name;
    private String place;
    private String tag;

}
