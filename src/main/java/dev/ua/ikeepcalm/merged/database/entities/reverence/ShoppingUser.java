package dev.ua.ikeepcalm.merged.database.entities.reverence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "shopping")
public class ShoppingUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userEntityId;
    @Column
    private Long userId;
    @ManyToOne
    private ReverenceChat channel;

    public void setUserEntityId(int userEntityId) {
        this.userEntityId = userEntityId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setChannel(ReverenceChat channel) {
        this.channel = channel;
    }
}

