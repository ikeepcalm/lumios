package dev.ua.ikeepcalm.lumios.telegram.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.InputFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaMessage {
    private int messageId;
    private Long chatId;
    private String label;
    private InputFile media;
}

