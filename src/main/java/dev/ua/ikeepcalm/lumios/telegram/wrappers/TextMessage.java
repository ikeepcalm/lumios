package dev.ua.ikeepcalm.lumios.telegram.wrappers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextMessage {
    private String text;
    private long chatId;
    private Integer messageId;
    private String parseMode;
    private String filePath;
    private ReplyKeyboard replyKeyboard;

    public static TextMessage plainText(long chatId, String text) {
        return TextMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }

    public static TextMessage markdown(long chatId, String text) {
        return TextMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();
    }

    public static TextMessage reply(long chatId, String text, int replyToMessageId) {
        return TextMessage.builder()
                .chatId(chatId)
                .text(text)
                .messageId(replyToMessageId)
                .build();
    }
}