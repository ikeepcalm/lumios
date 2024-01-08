package dev.ua.ikeepcalm.queueupnow.database.entities.reverence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reverence_users")
public class ReverenceUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userEntityId;
    @Column
    private Long userId;
    @Column
    private String username;
    @Column(columnDefinition = "integer default 0")
    private int reverence;
    @Column(columnDefinition = "integer default 100")
    private int credits;
    @Column(columnDefinition = "integer default 100")
    private int sustainable;
    @Column(columnDefinition = "integer default 0")
    private int balance;
    @ManyToOne
    private ReverenceChat channel;

}

