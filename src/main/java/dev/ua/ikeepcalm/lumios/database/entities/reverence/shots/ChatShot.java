package dev.ua.ikeepcalm.lumios.database.entities.reverence.shots;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "records_chat_shot")
public class ChatShot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    private List<UserShot> userShots;

    @OneToOne
    private ReverenceChat reverenceChat;

    @Column
    private LocalDate date;

}

