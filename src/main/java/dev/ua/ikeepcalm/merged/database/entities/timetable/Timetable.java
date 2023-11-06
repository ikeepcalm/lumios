package dev.ua.ikeepcalm.merged.database.entities.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.WeekType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Entity
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    @JsonProperty("weekType")
    private WeekType weekType;

    @JoinColumn
    @OneToMany(mappedBy = "timeTable", cascade = CascadeType.ALL)
    @JsonProperty
    private List<DayEntry> days;

    @JoinColumn
    @ManyToOne
    private ReverenceChat chat;

}
