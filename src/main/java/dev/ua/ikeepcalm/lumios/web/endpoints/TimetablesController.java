package dev.ua.ikeepcalm.lumios.web.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.wrappers.TimetableWrapper;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.modules.impl.timetable.utils.TimetableParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/timetables")
public class TimetablesController {

    private final TimetableService timetableService;
    private final ChatService chatService;

    public TimetablesController(TimetableService timetableService, ChatService chatService) {
        this.timetableService = timetableService;
        this.chatService = chatService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createTimetable(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");
        }

        try {
            List<TimetableEntry> timetableEntries = TimetableParser.parseTimetableMessage(json);

            if (timetableEntries.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format! No timetable entries found!");

            for (TimetableEntry timetableEntry : timetableEntries) {
                timetableEntry.setChat(reverenceChat);
                timetableService.save(timetableEntry);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved list of given timetables!");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateTimetable(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        try {
            chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not found");
        }
        List<TimetableEntry> timetableEntries;
        try {
            timetableEntries = TimetableParser.parseTimetableMessage(json);

            if (timetableEntries.isEmpty()) {
                Logger.getGlobal().log(java.util.logging.Level.WARNING, "Invalid JSON body format! No timetable entries found!");
                Logger.getGlobal().log(java.util.logging.Level.WARNING, json);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format! No timetable entries found!");
            }

            for (TimetableEntry timetableEntry : timetableEntries) {
                try {
                    timetableEntry.setChat(chatService.findByChatId(chatId));
                    TimetableEntry optionalTimetableEntry = timetableService.findByChatIdAndWeekType(chatId, timetableEntry.getWeekType());
                    timetableService.delete(optionalTimetableEntry);
                    timetableService.save(timetableEntry);
                } catch (NoSuchEntityException e) {
                    timetableService.save(timetableEntry);
                }
            }
            Logger.getGlobal().log(java.util.logging.Level.INFO, "Successfully updated list of given timetables!");
            return ResponseEntity.status(HttpStatus.OK).body("Successfully updated list of given timetables!");

        } catch (IOException e) {
            Logger.getGlobal().log(java.util.logging.Level.WARNING, "Invalid JSON body format!");
            Logger.getGlobal().log(java.util.logging.Level.WARNING, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON body format!");
        }

    }

    @GetMapping("/retrieve")
    public ResponseEntity<List<TimetableWrapper>> getTimetablesByChatId(@RequestHeader("chatId") Long chatId) {
        List<TimetableEntry> timetableEntries;
        List<TimetableWrapper> timetableWrappers = new ArrayList<>();
        try {
            timetableEntries = timetableService.findAllByChatId(chatId);
            for (TimetableEntry timetableEntry : timetableEntries) {
                timetableWrappers.add(new TimetableWrapper(timetableEntry));
            }
            return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTimetable(@RequestHeader("chatId") Long chatId) {
        try {
            List<TimetableEntry> timetableEntry = timetableService.findAllByChatId(chatId);
            timetableService.deleteAll(timetableEntry);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted timetable for chat with ID: " + chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Timetable for chat with ID: " + chatId + " is not found");
        }
    }

}
