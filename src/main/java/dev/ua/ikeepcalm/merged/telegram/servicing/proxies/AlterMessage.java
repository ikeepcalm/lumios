/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
 */
package dev.ua.ikeepcalm.merged.telegram.servicing.proxies;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class AlterMessage {
    private int messageId;
    private Long chatId;
    private String text;
    private String parseMode;
    private ReplyKeyboard replyKeyboard;
    private String filePath;

    public int getMessageId() {
        return this.messageId;
    }

    public Long getChatId() {
        return this.chatId;
    }

    public String getText() {
        return this.text;
    }

    public String getParseMode() {
        return this.parseMode;
    }

    public ReplyKeyboard getReplyKeyboard() {
        return this.replyKeyboard;
    }

    public String getFilePath() {
        return this.filePath;
    }

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

