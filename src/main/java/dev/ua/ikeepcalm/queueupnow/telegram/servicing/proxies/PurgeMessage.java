package dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurgeMessage {

    @NotNull
    private int messageId;
    @NotNull
    private long chatId;

    public PurgeMessage(int messageId, long chatId) {
        this.messageId = messageId;
        this.chatId = chatId;
    }
}
