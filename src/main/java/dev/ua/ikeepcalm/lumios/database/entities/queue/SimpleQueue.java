package dev.ua.ikeepcalm.lumios.database.entities.queue;

import dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers.QueueWrapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "simpleQueues")
public class SimpleQueue {

    @Id
    @Column
    private UUID id;

    @Column
    private String alias;

    @Column
    private int messageId;

    @Column
    private long chatId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "simpleQueue", fetch = FetchType.LAZY)
    private List<SimpleUser> contents = new LinkedList<>();

    public SimpleQueue() {
        this.alias = "СТАНДАРТНА ЧЕРГА";
        this.id = UUID.randomUUID();
    }

    public SimpleQueue(String alias) {
        this.alias = alias;
        this.id = UUID.randomUUID();
    }

    public SimpleQueue(QueueWrapper queueWrapper) {
        this.alias = queueWrapper.getAlias();
        this.messageId = queueWrapper.getMessageId();
        this.id = queueWrapper.getId();
        this.contents = queueWrapper.unwrapContents();
        this.chatId = queueWrapper.getChatId();
    }

    public boolean flushUser(SimpleUser simpleUser) {
        return this.contents.getFirst().equals(simpleUser);
    }

    @Override
    public String toString() {
        return "SimpleQueue{" +
               "id=" + id +
               ", alias='" + alias + '\'' +
               ", messageId=" + messageId +
               ", chatId=" + chatId +
               ", contents=" + contents +
               '}';
    }
}

