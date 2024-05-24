package dev.ua.ikeepcalm.lumios.database.entities.queue;

import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.UserWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MixedUser mixedUser = (MixedUser) o;
        return Objects.equals(this.username, mixedUser.username) && Objects.equals(this.name, mixedUser.name) && Objects.equals(this.accountId, mixedUser.accountId);
    }

    public int hashCode() {
        return Objects.hash(this.username, this.name, this.accountId);
    }

    public String toString() {
        return "User{username='" + this.username + "', name='" + this.name + "', accountId=" + this.accountId + "}";
    }
}

