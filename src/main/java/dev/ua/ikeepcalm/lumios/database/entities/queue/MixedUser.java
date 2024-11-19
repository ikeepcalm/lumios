package dev.ua.ikeepcalm.lumios.database.entities.queue;

import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.UserWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "mixedUsers")
public class MixedUser {

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
    @JoinColumn(name = "mixedQueue")
    private MixedQueue mixedQueue;

    public MixedUser() {
    }

    public MixedUser(UserWrapper userWrapper) {
        this.username = userWrapper.getUsername();
        this.name = userWrapper.getName();
        this.accountId = userWrapper.getAccountId();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MixedUser && ((MixedUser) obj).getAccountId().equals(this.accountId);
    }

    public String toString() {
        return "User{username='" + this.username + "', name='" + this.name + "', accountId=" + this.accountId + "}";
    }
}

