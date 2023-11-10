package dev.ua.ikeepcalm.merged.restapi;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.wrappers.TimetableWrapper;
import dev.ua.ikeepcalm.merged.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.merged.telegram.modules.timetable.utils.TimetableParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    private final TimetableService timetableService;
    private final ChatService chatService;

    public TimetableController(TimetableService timetableService, ChatService chatService) {
        this.timetableService = timetableService;
        this.chatService = chatService;
    }


    @PostMapping("/feed")
    public ResponseEntity<String> saveTimetable(@RequestBody String json, @RequestHeader("chatId") Long chatId) {
        ReverenceChat reverenceChat;
        try {
            reverenceChat = chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");

        }

        try {
            TimetableEntry timetableEntry = TimetableParser.parseTimetableMessage(json);
            timetableEntry.setChat(reverenceChat);
            timetableService.save(timetableEntry);
            return ResponseEntity.status(HttpStatus.CREATED).body("Timetable saved with ID: " + timetableEntry.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateTimetable(@RequestBody String json, @RequestHeader("chatId") Long chatId) {

        try {
            chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat with ID: " + chatId + " is not registered in the system");

        }
        try {
            TimetableEntry timetableEntry = TimetableParser.parseTimetableMessage(json);
            try {
                TimetableEntry optionalTimetableEntry = timetableService.findByChatIdAndWeekType(chatId, timetableEntry.getWeekType());
                timetableService.delete(optionalTimetableEntry);
                timetableService.save(timetableEntry);
                return ResponseEntity.status(HttpStatus.OK).body("Timetable updated with ID: " + timetableEntry.getId());
            } catch (NoSuchEntityException e) {
                timetableService.save(timetableEntry);
                return ResponseEntity.status(HttpStatus.OK).body("There was timetable with id: " + chatId + ". It was created instead");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format");
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
            } return ResponseEntity.status(HttpStatus.OK).body(timetableWrappers);
        } catch (NoSuchEntityException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
