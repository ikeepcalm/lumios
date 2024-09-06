package dev.ua.ikeepcalm.lumios.database.entities.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.records.MessageRecord;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class LumiosUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userEntityId;
    @Column
    private Long userId;
    @Column
    private String username;
    @Column
    private String fullName;
    @Column(columnDefinition = "integer default 0")
    private int reverence;
    @Column(columnDefinition = "integer default 100")
    private int credits;
    @Column(columnDefinition = "integer default 100")
    private int sustainable;
    @Column(columnDefinition = "integer default 0")
    private int balance;
    @ManyToOne
    private LumiosChat chat;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageRecord> messages;
}

