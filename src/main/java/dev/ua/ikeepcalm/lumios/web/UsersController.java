package dev.ua.ikeepcalm.lumios.web;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.ChatWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.UserWrapper;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final ChatService chatService;
    private final UserService userService;
    private final TelegramClient telegramClient;

    public UsersController(ChatService chatService, UserService userService, TelegramClient telegramClient) {
        this.chatService = chatService;
        this.userService = userService;
        this.telegramClient = telegramClient;
    }

    @GetMapping("{id}")
    public ResponseEntity<List<UserWrapper>> getUser(@PathVariable long id) {
        Iterable<ReverenceChat> reverenceChats = chatService.findAll();
        List<ReverenceUser> users = userService.findById(id);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        UserWrapper userWrapper = new UserWrapper();
        userWrapper.setUsername(users.getFirst().getUsername());
        userWrapper.setAccountId(users.getFirst().getUserId());

        for (ReverenceUser iteUser : users) {
            ChatWrapper chatWrapper = new ChatWrapper();
            chatWrapper.setId(iteUser.getChannel().getChatId());
            if (iteUser.getUserId().equals(iteUser.getChannel().getChatId())) {
                chatWrapper.setAdmin(true);
            } else {
                List<ChatMember> telegramUsers;
                try {
                    telegramUsers = telegramClient.getChatAdministrators(String.valueOf(iteUser.getChannel().getChatId()));
                } catch (TelegramApiException e) {
                    continue;
                }
                for (ChatMember chatMember : telegramUsers) {
                    if (chatMember.getUser().getId().equals(iteUser.getUserId())) {
                        userWrapper.setName(chatMember.getUser().getFirstName() + " " + chatMember.getUser().getLastName());
                        chatWrapper.setAdmin(true);
                    }
                }
            }

            userWrapper.addChat(chatWrapper);
        }

        return ResponseEntity.status(HttpStatus.OK).body(List.of(userWrapper));
    }

}
