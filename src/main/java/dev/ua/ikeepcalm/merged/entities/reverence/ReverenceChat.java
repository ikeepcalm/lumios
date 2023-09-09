/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.OneToMany
 *  jakarta.persistence.Table
 */
package dev.ua.ikeepcalm.merged.entities.reverence;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="chats")
public class ReverenceChat {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long chatId;
    @OneToMany(mappedBy="channel")
    private Set<ReverenceUser> users = new HashSet<ReverenceUser>();

    public Long getId() {
        return this.id;
    }

    public Long getChatId() {
        return this.chatId;
    }

    public Set<ReverenceUser> getUsers() {
        return this.users;
    }

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

