package dev.ua.ikeepcalm.lumios.telegram.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextMessage {
    private String text;
    private long chatId;
    private int messageId;
    private String parseMode;
    private String filePath;
    private ReplyKeyboard replyKeyboard;
}

