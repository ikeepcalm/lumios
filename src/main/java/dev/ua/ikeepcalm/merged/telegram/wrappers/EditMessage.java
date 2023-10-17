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
public class EditMessage {
    private int messageId;
    private Long chatId;
    private String text;
    private String parseMode;
    private ReplyKeyboard replyKeyboard;
    private String filePath;

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setParseMode(String parseMode) {
        this.parseMode = parseMode;
    }

    public void setReplyKeyboard(ReplyKeyboard replyKeyboard) {
        this.replyKeyboard = replyKeyboard;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

