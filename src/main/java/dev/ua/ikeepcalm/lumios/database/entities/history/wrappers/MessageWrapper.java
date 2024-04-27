package dev.ua.ikeepcalm.lumios.database.entities.history.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.lumios.database.entities.history.MessageRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageWrapper {

    @JsonProperty("messageId")
    private Long messageId;

    @JsonProperty("chatId")
    private Long chatId;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("username")
    private String username;

    public MessageWrapper(MessageRecord messageRecord) {
        this.messageId = messageRecord.getMessageId();
        this.chatId = messageRecord.getChatId();
        this.date = messageRecord.getDate();
        this.username = messageRecord.getUser().getUsername();
    }
}
