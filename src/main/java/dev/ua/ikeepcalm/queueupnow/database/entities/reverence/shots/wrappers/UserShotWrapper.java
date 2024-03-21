package dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserShotWrapper {

    @JsonProperty("username")
    private String username;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("reverence")
    private int reverence;

}
