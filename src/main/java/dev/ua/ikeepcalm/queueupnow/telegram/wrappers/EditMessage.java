package dev.ua.ikeepcalm.queueupnow.telegram.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditMessage {
    private int messageId;
    private Long chatId;
    private String text;
    private String parseMode;
    private ReplyKeyboard replyKeyboard;
    private String filePath;
}

