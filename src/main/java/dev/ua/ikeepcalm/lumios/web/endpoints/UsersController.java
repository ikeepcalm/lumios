package dev.ua.ikeepcalm.lumios.web.endpoints;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.BindService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceBind;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.ChatWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.UserWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoBindSpecifiedException;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class UsersController {

    private final ChatService chatService;
    private final UserService userService;
    private final TelegramClient telegramClient;
    private final BindService bindService;

    public UsersController(ChatService chatService, UserService userService, TelegramClient telegramClient, BindService bindService) {
        this.chatService = chatService;
        this.userService = userService;
        this.telegramClient = telegramClient;
        this.bindService = bindService;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserWrapper> getUser(@PathVariable long id) {
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
            chatWrapper.setName(iteUser.getChannel().getName());
            try {
                Chat chat = telegramClient.getChat(String.valueOf(iteUser.getChannel().getChatId()));
                chatWrapper.setDescription(chat.getDescription());
            } catch (TelegramApiException e) {
                continue;
            }
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

        return ResponseEntity.status(HttpStatus.OK).body(userWrapper);
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String id) {
        try {
            Chat chat = telegramClient.getChat(id);
            File photoFile = telegramClient.downloadFile(telegramClient.getFile(chat.getPhoto().getBigFileId()));
            byte[] photoBytes = Files.readAllBytes(photoFile.toPath());
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photoBytes);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unbind/{id}")
    public ResponseEntity<String> getChat(@PathVariable long id) {
        try {
            BindResult result = retrieveBind(id);
            return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(result.chat.getChatId()));
        } catch (NoBindSpecifiedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Seems like the bind hasn't been set for this user!");
        }
    }

    @Transactional
    @PostMapping("/bind/{id}")
    public ResponseEntity<String> bindUser(@PathVariable long id, @RequestHeader("chatId") long chatId) {
        ReverenceChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        ReverenceUser user;
        try {
            user = userService.findById(id, chat);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        bindService.deleteByUserId(id);
        ReverenceBind reverenceBind = new ReverenceBind();
        reverenceBind.setChatId(chat.getChatId());
        reverenceBind.setUserId(user.getUserId());
        bindService.save(reverenceBind);

        return ResponseEntity.status(HttpStatus.OK).body("User bound!");
    }

    private BindResult retrieveBind(long userId) throws NoBindSpecifiedException {
        ReverenceBind bind;

        try {
            bind = bindService.findByUserId(userId);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No bind specified for user " + userId);
        }

        ReverenceChat chat;
        try {
            chat = chatService.findByChatId(bind.getChatId());
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No chat specified for user bind " + bind.getUserId());
        }

        ReverenceUser user;
        try {
            user = userService.findById(userId, chat);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No user specified for user bind " + bind.getUserId());
        }

        return new BindResult(user, chat);
    }

    private record BindResult(ReverenceUser user, ReverenceChat chat) {
    }

}
