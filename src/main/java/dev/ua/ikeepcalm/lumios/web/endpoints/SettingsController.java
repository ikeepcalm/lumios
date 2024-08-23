package dev.ua.ikeepcalm.lumios.web.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.wrappers.SettingsWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.SettingsParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private ChatService chatService;

    public SettingsController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/current")
    public ResponseEntity<SettingsWrapper> getSettings(@RequestParam("chatId") Long chatId) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new SettingsWrapper(chat));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateSettings(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        SettingsWrapper settings;
        try {
            settings = SettingsParser.parseSettings(json);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        chat.setTimetableEnabled(settings.isTimetable());
        chat.setDiceEnabled(settings.isDice());

        chatService.save(chat);

        return ResponseEntity.status(HttpStatus.OK).body("Settings updated");

    }


}
