/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 */
package dev.ua.ikeepcalm.merged.dal.impls;

import dev.ua.ikeepcalm.merged.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.dal.repositories.ChatRepository;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl
implements ChatService {
    @Autowired
    private ChatRepository chatRepository;

    @Override
    public ReverenceChat find(long chatId) {
        return this.chatRepository.findByChatId(chatId);
    }

    @Override
    public void save(ReverenceChat chat) {
        this.chatRepository.save(chat);
    }

    @Override
    public void delete(ReverenceChat chat) {
        this.chatRepository.save(chat);
    }
}
