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
@Table(name="users")
public class ReverenceUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int userEntityId;
    @Column
    private Long userId;
    @Column
    private String username;
    @Column(columnDefinition="integer default 0")
    private int reverence;
    @Column(columnDefinition="integer default 100")
    private int credits;
    @Column(columnDefinition="integer default 100")
    private int sustainable;
    @Column(columnDefinition="integer default 0")
    private int balance;
    @ManyToOne
    private ReverenceChat channel;

    public int getUserEntityId() {
        return this.userEntityId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public int getReverence() {
        return this.reverence;
    }

    public int getCredits() {
        return this.credits;
    }

    public int getSustainable() {
        return this.sustainable;
    }

    public int getBalance() {
        return this.balance;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setReverence(int reverence) {
        this.reverence = reverence;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setSustainable(int sustainable) {
        this.sustainable = sustainable;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setChannel(ReverenceChat channel) {
        this.channel = channel;
    }
}

