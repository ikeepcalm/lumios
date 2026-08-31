package dev.ua.ikeepcalm.lumios.database.entities.timetable.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampusClass {

    private String type;
    private String time;
    private String name;
    private String tag;

    /**
     * Campus nests the teacher under {@code lecturer}, and names the room {@code location} - the
     * flat {@code teacherName}/{@code place} fields this class used to declare matched no key in the
     * payload and were therefore always null.
     */
    private Lecturer lecturer;
    private String location;

    public String teacherName() {
        return lecturer == null ? null : lecturer.getName();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lecturer {
        private String id;
        private String name;
    }

}
