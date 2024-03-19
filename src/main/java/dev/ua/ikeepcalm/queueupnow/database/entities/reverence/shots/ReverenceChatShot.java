package dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "reverence_shots")
public class ReverenceChatShot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @OneToMany
    private List<ReverenceUserShot> users;

    @ManyToOne
    private ReverenceChat reverenceChat;

    @Column
    private LocalDate date;

}

