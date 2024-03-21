package dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "user_shots")
public class UserShot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private Long userId;

    @Column
    private int reverence;

}
