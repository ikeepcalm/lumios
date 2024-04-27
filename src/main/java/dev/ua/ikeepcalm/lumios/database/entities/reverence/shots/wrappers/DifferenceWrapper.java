package dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifferenceWrapper {

    @JsonProperty("username")
    private String username;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("difference")
    private int reverence;

    public DifferenceWrapper(UserShot shot, UserShot endUserShot) {
        this.username = shot.getUsername();
        this.userId = shot.getUserId();
        this.reverence = endUserShot.getReverence() - shot.getReverence();
    }
}

