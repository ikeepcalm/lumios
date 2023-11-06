package dev.ua.ikeepcalm.merged.database.entities.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.ClassType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JsonProperty("className")
    private String name;

    @Column
    @JsonProperty("url")
    private String url;

    @Column
    @Enumerated(EnumType.STRING)
    @JsonProperty("classType")
    private ClassType classType;

    @Column
    @JsonProperty("startTime")
    private LocalTime startTime;

    @Column
    @JsonProperty("endTime")
    private LocalTime endTime;

    @JoinColumn(name = "day_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private DayEntry dayEntry;

}
