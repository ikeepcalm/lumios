/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 */
package dev.ua.ikeepcalm.merged.entities.reverence;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="shopping")
public class ShoppingUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int userEntityId;
    @Column
    private Long userId;
    @ManyToOne
    private ReverenceChat channel;

    public int getUserEntityId() {
        return this.userEntityId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public ReverenceChat getChannel() {
        return this.channel;
    }

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

