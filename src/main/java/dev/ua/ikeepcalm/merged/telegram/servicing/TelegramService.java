/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.CallbackQuery
 *  org.telegram.telegrambots.meta.api.objects.Message
 *  org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
 */
package dev.ua.ikeepcalm.merged.telegram.servicing;

import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.AlterMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.MultiMessage;
import dev.ua.ikeepcalm.merged.telegram.servicing.proxies.PurgeMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramService {
    public void deleteCallbackMessage(CallbackQuery var1);

    public void sendCallbackMessage(CallbackQuery var1, String var2);

    public Message sendMultiMessage(MultiMessage var1);

    public void sendAnswerCallbackQuery(String var1, String var2);

    public void sendForwardMessage(Message var1, long var2);

    public Message sendAlterMessage(AlterMessage var1);

    public void sendPurgeMessage(PurgeMessage var1);

    public void pinChatMessage(long var1, long var3);

    public InlineKeyboardMarkup createMarkup(String[] var1, String var2);

    public void sendVideo(long var1, String var3, int var4);

    public void unpinChatMessage(long var1, long var3);
}

