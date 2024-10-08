package dev.ua.ikeepcalm.lumios.database.entities.queue;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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
    String fullName;

    @Column
    private Long accountId;

    @ManyToOne
    @JoinColumn(name = "simpleQueue")
    private SimpleQueue simpleQueue;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleUser mixedUser = (SimpleUser) o;
        return Objects.equals(this.username, mixedUser.username) && Objects.equals(this.name, mixedUser.name) && Objects.equals(this.accountId, mixedUser.accountId);
    }

    public int hashCode() {
        return Objects.hash(this.username, this.name, this.accountId);
    }

    public String toString() {
        return "User{username='" + this.username + "', name='" + this.name + "', accountId=" + this.accountId + "}";
    }
}

