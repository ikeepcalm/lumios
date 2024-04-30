package dev.ua.ikeepcalm.lumios.database.entities.queue.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
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
    @JsonProperty("isMixed")
    private boolean isMixed;

    public static List<QueueWrapper> wrapSimpleQueues(List<SimpleQueue> queues) {
        List<QueueWrapper> queueWrappers = new ArrayList<>();
        for (SimpleQueue queue : queues) {
            QueueWrapper queueWrapper = wrapQueue(queue);
            queueWrappers.add(queueWrapper);
        }
        return queueWrappers;
    }

    public static List<QueueWrapper> wrapMixedQueues(List<MixedQueue> queues) {
        List<QueueWrapper> queueWrappers = new ArrayList<>();
        for (MixedQueue queue : queues) {
            QueueWrapper queueWrapper = wrapQueue(queue);
            queueWrappers.add(queueWrapper);
        }
        return queueWrappers;
    }

    public static List<QueueWrapper> wrapQueues(List<SimpleQueue> simpleQueues, List<MixedQueue> mixedQueues) {
        List<QueueWrapper> queueWrappers = QueueWrapper.wrapSimpleQueues(simpleQueues);
        for (MixedQueue queue : mixedQueues) {
            QueueWrapper queueWrapper = wrapQueue(queue);
            queueWrappers.add(queueWrapper);
        }
        return queueWrappers;
    }

    public static QueueWrapper wrapQueue(SimpleQueue queue) {
        QueueWrapper queueWrapper = new QueueWrapper();
        queueWrapper.setAlias(queue.getAlias());
        queueWrapper.setMessageId(queue.getMessageId());
        queueWrapper.setId(queue.getId());
        queueWrapper.setChatId(queue.getChatId());
        queueWrapper.setMixed(false);
        for (SimpleUser user : queue.getContents()) {
            queueWrapper.getContents().add(UserWrapper.wrapUsers(user));
        }
        return queueWrapper;
    }

    public static QueueWrapper wrapQueue(MixedQueue queue) {
        QueueWrapper queueWrapper = new QueueWrapper();
        queueWrapper.setAlias(queue.getAlias());
        queueWrapper.setMessageId(queue.getMessageId());
        queueWrapper.setId(queue.getId());
        queueWrapper.setChatId(queue.getChatId());
        queueWrapper.setMixed(true);
        for (MixedUser user : queue.getContents()) {
            queueWrapper.getContents().add(UserWrapper.wrapUsers(user));
        }
        return queueWrapper;
    }

    public List<SimpleUser> unwrapContents() {
        List<SimpleUser> simpleUsers = new ArrayList<>();
        for (UserWrapper userWrapper : this.contents) {
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setUsername(userWrapper.getUsername());
            simpleUser.setName(userWrapper.getName());
            simpleUser.setAccountId(userWrapper.getAccountId());
            simpleUsers.add(simpleUser);
        }
        return simpleUsers;
    }

    public QueueWrapper() {
        this.contents = new ArrayList<>();
    }
}
