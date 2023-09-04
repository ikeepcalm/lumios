package dev.ua.ikeepcalm.queueupnow.telegram.servicing;


import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.queueupnow.telegram.servicing.proxies.PurgeMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;

public interface TelegramService{

    Message sendMultiMessage(MultiMessage multiMessage);

    void sendAnswerCallbackQuery(String text, String callbackQueryId);

    void sendForwardMessage(Message origin, long chatId);

    Message sendAlterMessage(AlterMessage alterMessage);

    void sendPurgeMessage(PurgeMessage purgeMessage);
    void pinChatMessage(long chatId, long messageId);
    void unpinChatMessage(long chatId, long messageId);
}
