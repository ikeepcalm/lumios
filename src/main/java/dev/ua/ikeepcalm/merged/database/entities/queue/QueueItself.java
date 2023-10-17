/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.database.entities.queue;

import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

@Getter
public class QueueItself
implements Serializable {
    private UUID id;
    private String alias;
    private long messageId;
    private Queue<QueueUser> contents = new LinkedList<>();

    public QueueItself() {
        this.alias = "СТАНДАРТНА ЧЕРГА";
        this.id  = UUID.randomUUID();
    }

    public QueueItself(String alias) {
        this.alias = alias;
        this.id = UUID.randomUUID();
    }

    public void addUser(QueueUser queueUser) {
        this.contents.add(queueUser);
    }

    public void removeUser(QueueUser queueUser) {
        this.contents.remove(queueUser);
    }

    public boolean flushUser(QueueUser queueUser) {
        if (this.contents.peek() != null && this.contents.peek().getAccountId().equals(queueUser.getAccountId())) {
            this.contents.poll();
            return true;
        }
        return false;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setContents(Queue<QueueUser> contents) {
        this.contents = contents;
    }
}

