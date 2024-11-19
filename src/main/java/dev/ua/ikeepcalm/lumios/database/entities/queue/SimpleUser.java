package dev.ua.ikeepcalm.lumios.database.entities.queue;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "simpleUsers")
public class SimpleUser {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String name;

    @Column
    private Long accountId;

    @ManyToOne
    @JoinColumn(name = "simpleQueue")
    private SimpleQueue simpleQueue;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SimpleUser && ((SimpleUser) obj).getAccountId().equals(this.accountId);
    }

    public String toString() {
        return "User{username='" + this.username + "', name='" + this.name + "', accountId=" + this.accountId + "}";
    }
}

