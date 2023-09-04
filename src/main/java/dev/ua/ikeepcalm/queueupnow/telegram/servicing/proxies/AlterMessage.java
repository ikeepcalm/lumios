package dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
@Setter
public class AlterMessage {
    private int messageId;
    private Long chatId;
    private String text;
    private String parseMode;
    private ReplyKeyboard replyKeyboard;
    private String filePath;
}
