package dev.ua.ikeepcalm.queueupnow.entities;


import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.UUID;

@Getter
@Setter
public class Queue {

    private UUID id;
    private String alias;
    private long messageId;
    private java.util.Queue<User> contents;

    public Queue() {
        this.id = UUID.randomUUID();
        this.alias = "СТАНДАРТНА ЧЕРГА";
        this.contents = new LinkedList<>();
    }

    public Queue(String alias) {
        this.id = UUID.randomUUID();
        this.alias = alias;
        this.contents = new LinkedList<>();
    }

    public void addUser(User user){
        this.contents.add(user);
    }

    public void removeUser(User user){
        this.contents.remove(user);
    }

    public boolean flushUser(User user){
        if (contents.peek().getAccountId().equals(user.getAccountId())){
            contents.poll();
            return true;
        } else {
            return false;
        }
    }

}
