package dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatShotWrapper {

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("userShots")
    private List<UserShotWrapper> userShots;

}
