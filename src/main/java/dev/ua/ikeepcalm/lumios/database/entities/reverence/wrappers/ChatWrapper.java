package dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatWrapper {

    @JsonProperty
    private long id;

    @JsonProperty
    private boolean admin;

}
