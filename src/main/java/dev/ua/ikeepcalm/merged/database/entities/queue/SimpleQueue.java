package dev.ua.ikeepcalm.merged.database.entities.queue;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

@Getter
@Setter
public class SimpleQueue
implements Serializable {
    private UUID id;
    private String alias;
    private int messageId;
    private Queue<QueueUser> contents = new LinkedList<>();

    public SimpleQueue() {
        this.alias = "СТАНДАРТНА ЧЕРГА";
        this.id  = UUID.randomUUID();
    }

    public SimpleQueue(String alias) {
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

    public void setContents(Queue<QueueUser> contents) {
        this.contents = contents;
    }
}

