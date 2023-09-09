/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotNull
 */
package dev.ua.ikeepcalm.merged.telegram.servicing.proxies;

import jakarta.validation.constraints.NotNull;

public class PurgeMessage {
    @NotNull
    private int messageId;
    @NotNull
    private long chatId;

    public PurgeMessage(int messageId, long chatId) {
        this.messageId = messageId;
        this.chatId = chatId;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public long getChatId() {
        return this.chatId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}

