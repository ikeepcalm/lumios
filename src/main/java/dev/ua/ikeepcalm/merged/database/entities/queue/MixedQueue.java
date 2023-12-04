package dev.ua.ikeepcalm.merged.database.entities.queue;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

@Getter
@Setter
public class MixedQueue
implements Serializable {
    private UUID id;
    private String alias;
    private int messageId;
    private boolean shuffled = false;
    private Queue<QueueUser> contents = new LinkedList<>();

    public MixedQueue() {
        this.alias = "ЗМІШАНА ЧЕРГА";
        this.id  = UUID.randomUUID();
    }

    public MixedQueue(String alias) {
        this.alias = alias;
        this.id = UUID.randomUUID();
    }

    public void addUser(QueueUser queueUser) {
        this.contents.add(queueUser);
    }

    public void shuffleContents() {
        Collections.shuffle((LinkedList<QueueUser>) this.contents);
        this.shuffled = true;
    }

    public void setContents(Queue<QueueUser> contents) {
        this.contents = contents;
    }
}

