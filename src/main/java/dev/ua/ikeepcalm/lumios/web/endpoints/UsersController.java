package dev.ua.ikeepcalm.lumios.web.endpoints;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.BindService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosBind;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.ChatWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.UserWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoBindSpecifiedException;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
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
        List<LumiosUser> users = userService.findById(id);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        UserWrapper userWrapper = new UserWrapper();
        userWrapper.setUsername(users.getFirst().getUsername());
        userWrapper.setAccountId(users.getFirst().getUserId());

        for (LumiosUser iteUser : users) {
            if (iteUser.getUserId().equals(iteUser.getChat().getChatId())) {
                continue;
            }
            ChatWrapper chatWrapper = new ChatWrapper();
            chatWrapper.setId(iteUser.getChat().getChatId());
            chatWrapper.setName(iteUser.getChat().getName());
            try {
                ChatFullInfo chat = telegramClient.getChat(String.valueOf(iteUser.getChat().getChatId()));
                chatWrapper.setDescription(chat.getDescription());
            } catch (TelegramApiException ignored) {
                continue;
            }
            if (iteUser.getUserId().equals(iteUser.getChat().getChatId())) {
                chatWrapper.setAdmin(true);
            } else {
                List<ChatMember> telegramUsers;
                try {
                    telegramUsers = telegramClient.getChatAdministrators(String.valueOf(iteUser.getChat().getChatId()));
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
            ChatFullInfo chat = telegramClient.getChat(id);
            if (chat.getPhoto() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            File photoFile = telegramClient.downloadFile(telegramClient.getFile(chat.getPhoto().getBigFileId()));
            byte[] photoBytes = Files.readAllBytes(photoFile.toPath());
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photoBytes);
        } catch (IOException | TelegramApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        LumiosUser user;
        try {
            user = userService.findById(id, chat);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        bindService.deleteByUserId(id);
        LumiosBind lumiosBind = new LumiosBind();
        lumiosBind.setChatId(chat.getChatId());
        lumiosBind.setUserId(user.getUserId());
        bindService.save(lumiosBind);

        return ResponseEntity.status(HttpStatus.OK).body("User bound!");
    }

    private BindResult retrieveBind(long userId) throws NoBindSpecifiedException {
        LumiosBind bind;

        try {
            bind = bindService.findByUserId(userId);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No bind specified for user " + userId);
        }

        LumiosChat chat;
        try {
            chat = chatService.findByChatId(bind.getChatId());
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No chat specified for user bind " + bind.getUserId());
        }

        LumiosUser user;
        try {
            user = userService.findById(userId, chat);
        } catch (NoSuchEntityException e) {
            throw new NoBindSpecifiedException("No user specified for user bind " + bind.getUserId());
        }

        return new BindResult(user, chat);
    }

    private record BindResult(LumiosUser user, LumiosChat chat) {
    }

}
