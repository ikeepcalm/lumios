/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.wrappers;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
public class TextMessage {
    private String text;
    private long chatId;
    private int messageId;
    private String filePath;
    private ReplyKeyboard replyKeyboard;

    public void setText(String text) {
        this.text = text;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setReplyKeyboard(ReplyKeyboard replyKeyboard) {
        this.replyKeyboard = replyKeyboard;
    }
}

