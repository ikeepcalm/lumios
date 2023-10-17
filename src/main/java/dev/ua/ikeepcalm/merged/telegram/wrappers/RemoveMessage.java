/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotNull
 */
package dev.ua.ikeepcalm.merged.telegram.wrappers;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RemoveMessage {
    @NotNull
    private int messageId;
    @NotNull
    private long chatId;

    public RemoveMessage(int messageId, long chatId) {
        this.messageId = messageId;
        this.chatId = chatId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}

