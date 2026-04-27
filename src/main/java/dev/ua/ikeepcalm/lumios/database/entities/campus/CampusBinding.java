package dev.ua.ikeepcalm.lumios.database.entities.campus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "campus_bindings")
public class CampusBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long telegramUserId;

    @Column(nullable = false, length = 2048)
    private String accessToken;

    @Column(nullable = false)
    private LocalDateTime subscribedAt;

}
