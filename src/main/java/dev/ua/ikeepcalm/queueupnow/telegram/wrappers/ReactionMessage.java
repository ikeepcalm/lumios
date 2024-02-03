package dev.ua.ikeepcalm.queueupnow.telegram.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReactionMessage {
    private int messageId;
    private Long chatId;
    private String filePath;
    private List<ReactionType> reactionTypes;
    private boolean isBig;
}

