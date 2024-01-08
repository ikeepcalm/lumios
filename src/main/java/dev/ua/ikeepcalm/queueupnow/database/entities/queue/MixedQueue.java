package dev.ua.ikeepcalm.queueupnow.database.entities.queue;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "mixed_queues")
public class MixedQueue {

    @Id
    @Column
    private UUID id;

    @Column
    private String alias;

    @Column
    private int messageId;

    @Column
    private boolean shuffled = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mixedQueue")
    private List<MixedUser> contents = new LinkedList<>();

    public MixedQueue() {
        this.alias = "ЗМІШАНА ЧЕРГА";
        this.id  = UUID.randomUUID();
    }

    public MixedQueue(String alias) {
        this.alias = alias;
        this.id = UUID.randomUUID();
    }

    public void shuffleContents() {
        Collections.shuffle(this.contents);
        this.shuffled = true;
    }

}

