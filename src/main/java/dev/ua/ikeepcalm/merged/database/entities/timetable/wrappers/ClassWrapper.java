package dev.ua.ikeepcalm.merged.database.entities.timetable.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.merged.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.ClassType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassWrapper {

    @JsonProperty("className")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("classType")
    private ClassType classType;

    @JsonProperty("startTime")
    private LocalTime startTime;

    @JsonProperty("endTime")
    private LocalTime endTime;

    public ClassWrapper(ClassEntry classEntry) {
        this.name = classEntry.getName();
        this.url = classEntry.getUrl();
        this.classType = classEntry.getClassType();
        this.startTime = classEntry.getStartTime();
        this.endTime = classEntry.getEndTime();
    }

    public ClassWrapper() {

    }

    public ClassWrapper(String name, String url, ClassType classType, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.url = url;
        this.classType = classType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
