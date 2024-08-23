package dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsWrapper {

    @JsonProperty
    private boolean timetable;

    @JsonProperty
    private boolean dice;

    public SettingsWrapper(LumiosChat chat) {
        this.timetable = chat.isTimetableEnabled();
        this.dice = chat.isDiceEnabled();
    }

}
