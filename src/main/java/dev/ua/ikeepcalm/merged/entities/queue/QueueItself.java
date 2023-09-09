/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.entities.queue;

import dev.ua.ikeepcalm.merged.entities.queue.QueueUser;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class QueueItself
implements Serializable {
    private UUID id = UUID.randomUUID();
    private String alias;
    private long messageId;
    private Queue<QueueUser> contents;

    public QueueItself() {
        this.alias = "\u0421\u0422\u0410\u041d\u0414\u0410\u0420\u0422\u041d\u0410 \u0427\u0415\u0420\u0413\u0410";
        this.contents = new LinkedList<QueueUser>();
    }

    public QueueItself(String alias) {
        this.alias = alias;
        this.contents = new LinkedList<QueueUser>();
    }

    public void addUser(QueueUser queueUser) {
        this.contents.add(queueUser);
    }

    public void removeUser(QueueUser queueUser) {
        this.contents.remove(queueUser);
    }

    public boolean flushUser(QueueUser queueUser) {
        if (this.contents.peek().getAccountId().equals(queueUser.getAccountId())) {
            this.contents.poll();
            return true;
        }
        return false;
    }

    public UUID getId() {
        return this.id;
    }

    public String getAlias() {
        return this.alias;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public Queue<QueueUser> getContents() {
        return this.contents;
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

