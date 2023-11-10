package dev.ua.ikeepcalm.merged.database.entities.reverence;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "chats")
public class ReverenceChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long chatId;

    @OneToMany(mappedBy = "channel")
    private Set<ReverenceUser> users = new HashSet<>();

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setUsers(Set<ReverenceUser> users) {
        this.users = users;
    }
}

