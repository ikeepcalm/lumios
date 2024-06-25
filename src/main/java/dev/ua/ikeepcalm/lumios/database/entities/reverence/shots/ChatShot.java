package dev.ua.ikeepcalm.lumios.database.entities.reverence.shots;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chatShots")
public class ChatShot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    private List<UserShot> userShots;

    @ManyToOne
    private LumiosChat chat;

    @Column
    private LocalDate date;

}

