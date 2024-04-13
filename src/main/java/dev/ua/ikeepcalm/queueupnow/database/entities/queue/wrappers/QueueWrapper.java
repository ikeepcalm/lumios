package dev.ua.ikeepcalm.queueupnow.database.entities.queue.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueWrapper {

    @JsonProperty("alias")
    private String alias;
    @JsonProperty("messageId")
    private int messageId;
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("chatId")
    private long chatId;
    @JsonProperty("contents")
    private List<UserWrapper> contents;

    public List<SimpleUser> unwrapContents() {
        List<SimpleUser> simpleUsers = new ArrayList<>();
        for (UserWrapper userWrapper : this.contents) {
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setUsername(userWrapper.getUsername());
            simpleUser.setName(userWrapper.getName());
            simpleUser.setAccountId(userWrapper.getAccountId());
            simpleUsers.add(simpleUser);
        } return simpleUsers;
    }

    public static List<QueueWrapper> wrapQueues(List<SimpleQueue> queues) {
        List<QueueWrapper> queueWrappers = new ArrayList<>();
        for (SimpleQueue queue : queues) {
            QueueWrapper queueWrapper = new QueueWrapper();
            queueWrapper.setAlias(queue.getAlias());
            queueWrapper.setMessageId(queue.getMessageId());
            queueWrapper.setId(queue.getId());
            queueWrapper.setChatId(queue.getChatId());
            for (SimpleUser user : queue.getContents()) {
                queueWrapper.getContents().add(UserWrapper.wrapUsers(user));
            }
            queueWrappers.add(queueWrapper);
        } return queueWrappers;
    }

    public static QueueWrapper wrapQueue(SimpleQueue queue) {
        QueueWrapper queueWrapper = new QueueWrapper();
        queueWrapper.setAlias(queue.getAlias());
        queueWrapper.setMessageId(queue.getMessageId());
        queueWrapper.setId(queue.getId());
        queueWrapper.setChatId(queue.getChatId());
        for (SimpleUser user : queue.getContents()) {
            queueWrapper.getContents().add(UserWrapper.wrapUsers(user));
        } return queueWrapper;
    }

}
