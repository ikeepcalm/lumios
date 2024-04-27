package dev.ua.ikeepcalm.lumios.web;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.RecordService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.entities.history.MessageRecord;
import dev.ua.ikeepcalm.lumios.database.entities.history.wrappers.MessageWrapper;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.UserShot;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.wrappers.DifferenceWrapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final RecordService recordService;
    private final ShotService shotService;

    public StatisticsController(RecordService recordService, ShotService shotService) {
        this.recordService = recordService;
        this.shotService = shotService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageWrapper>> getMessages(
            @RequestParam("chatId") Long chatId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MessageRecord> messageRecords = recordService.findAllByChatIdAndDateBetween(chatId, startDate, endDate);
        List<MessageWrapper> wrappers = new ArrayList<>();

        for (MessageRecord messageRecord : messageRecords) {
            MessageWrapper wrapper = new MessageWrapper(messageRecord);
            wrappers.add(wrapper);
        }

        return ResponseEntity.status(HttpStatus.OK).body(wrappers);
    }

    @GetMapping("/shots")
    public ResponseEntity<List<DifferenceWrapper>> getShots(
            @RequestParam("chatId") Long chatId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ChatShot startShot;
        ChatShot endShot;
        try {
            startShot = shotService.findByChatIdAndDate(chatId, startDate);
            endShot = shotService.findByChatIdAndDate(chatId, endDate);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<DifferenceWrapper> wrappers = new ArrayList<>();

        for (UserShot shot : startShot.getUsers()) {
            UserShot endUserShot = endShot.getUsers().stream().filter(userShot -> userShot.getId().equals(shot.getId())).findFirst().orElse(shot);
            DifferenceWrapper wrapper = new DifferenceWrapper(shot, endUserShot);
            wrappers.add(wrapper);
        }

        return ResponseEntity.status(HttpStatus.OK).body(wrappers);
    }

}
