package dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWrapper {

    @JsonProperty
    private String username;

    @JsonProperty
    private String name;

    @JsonProperty
    private Long accountId;

    @JsonProperty
    private List<ChatWrapper> chats;

    public UserWrapper() {
        chats = new ArrayList<>();
    }

    public void addChat(ChatWrapper chat) {
        chats.add(chat);
    }

}
