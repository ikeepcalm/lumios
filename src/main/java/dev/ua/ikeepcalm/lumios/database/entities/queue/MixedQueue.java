package dev.ua.ikeepcalm.lumios.database.entities.queue;

import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.UserWrapper;
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
    private long chatId;

    @Column
    private boolean shuffled = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mixedQueue")
    private List<MixedUser> contents = new LinkedList<>();

    public MixedQueue() {
        this.alias = "ЗМІШАНА ЧЕРГА";
        this.id = UUID.randomUUID();
    }

    public MixedQueue(String alias) {
        this.alias = alias;
        this.id = UUID.randomUUID();
    }

    public MixedQueue(QueueWrapper queueWrapper) {
        this.alias = queueWrapper.getAlias();
        this.id = queueWrapper.getId();
        this.messageId = queueWrapper.getMessageId();
        this.chatId = queueWrapper.getChatId();
        this.shuffled = queueWrapper.isMixed();
        for (UserWrapper userWrapper : queueWrapper.getContents()) {
            this.contents.add(new MixedUser(userWrapper));
        }
    }

    public void shuffleContents() {
        Collections.shuffle(this.contents);
        this.shuffled = true;
    }

    @Override
    public String toString() {
        return "MixedQueue{" +
               "id=" + id +
               ", alias='" + alias + '\'' +
               ", messageId=" + messageId +
               ", chatId=" + chatId +
               ", shuffled=" + shuffled +
               ", contents=" + contents +
               '}';
    }
}

