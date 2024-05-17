package dev.ua.ikeepcalm.lumios.database.entities.reverence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "reverence_chats")
public class ReverenceChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long chatId;

    @Column
    private String name;

    @OneToMany(mappedBy = "channel")
    private Set<ReverenceUser> users;

}

