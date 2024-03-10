package dev.ua.ikeepcalm.queueupnow.database.entities.queue.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWrapper {


    @JsonProperty("username")
    private String username;

    @JsonProperty("name")
    private String name;

    @JsonProperty("accountId")
    private Long accountId;

    public static UserWrapper wrapUsers(SimpleUser user) {
        UserWrapper userWrapper = new UserWrapper();
        userWrapper.setUsername(user.getUsername());
        userWrapper.setName(user.getName());
        userWrapper.setAccountId(user.getAccountId());
        return userWrapper;
    }

}
