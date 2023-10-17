/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.database.entities.queue;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class QueueUser
implements Serializable {
    private String username;
    private String name;
    private Long accountId;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        QueueUser queueUser = (QueueUser)o;
        return Objects.equals(this.username, queueUser.username) && Objects.equals(this.name, queueUser.name) && Objects.equals(this.accountId, queueUser.accountId);
    }

    public int hashCode() {
        return Objects.hash(this.username, this.name, this.accountId);
    }

    public String toString() {
        return "User{username='" + this.username + "', name='" + this.name + "', accountId=" + this.accountId + "}";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}

