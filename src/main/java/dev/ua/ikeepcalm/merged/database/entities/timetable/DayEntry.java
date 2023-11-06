package dev.ua.ikeepcalm.merged.database.entities.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayEntry {

    @Id
    private Long id;

    @Column
    @JsonProperty("dayName")
    private String dayName;

    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    @JoinColumn
    @OneToMany
    @JsonProperty("classEntries")
    private List<ClassEntry> classEntries;

}