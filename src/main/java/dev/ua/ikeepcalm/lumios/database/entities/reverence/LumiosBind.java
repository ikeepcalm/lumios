package dev.ua.ikeepcalm.lumios.database.entities.reverence;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "binds")
public class LumiosBind {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long chatId;

    @Column
    private Long userId;

}
