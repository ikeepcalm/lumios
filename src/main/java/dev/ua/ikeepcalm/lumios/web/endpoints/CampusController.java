package dev.ua.ikeepcalm.lumios.web.endpoints;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import dev.ua.ikeepcalm.lumios.web.endpoints.campus.GradeEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/campus")
public class CampusController {

    private final CampusBindingService campusBindingService;
    private final TelegramClient telegramClient;

    public CampusController(CampusBindingService campusBindingService, TelegramClient telegramClient) {
        this.campusBindingService = campusBindingService;
        this.telegramClient = telegramClient;
    }

    /**
     * Receives grade notification batches pushed by the campus system.
     * Campus sends a bare JSON array; each entry's {@code id} field is the Telegram user ID.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveGrades(@RequestBody List<GradeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        log.info("Received {} grade notification(s) from campus", entries.size());

        for (GradeEntry entry : entries) {
            try {
                deliverGradeNotification(entry);
            } catch (Exception e) {
                log.error("Failed to deliver grade notification for id={}: {}", entry.getId(), e.getMessage());
            }
        }

        return ResponseEntity.ok().build();
    }

    private void deliverGradeNotification(GradeEntry entry) {
        if (entry.getId() == null || entry.getId().isBlank()) {
            log.warn("Received grade entry with no id, skipping");
            return;
        }

        long telegramUserId;
        try {
            telegramUserId = Long.parseLong(entry.getId());
        } catch (NumberFormatException e) {
            log.warn("Grade entry id is not a valid Telegram user ID: {}", entry.getId());
            return;
        }

        try {
            campusBindingService.findByTelegramUserId(telegramUserId);
        } catch (NoSuchEntityException e) {
            log.debug("No campus binding found for telegramUserId={}, skipping", telegramUserId);
            return;
        }

        TextMessage message = new TextMessage();
        message.setChatId(telegramUserId);
        message.setText(buildNotificationText(entry));
        telegramClient.sendTextMessage(message);
    }

    private String buildNotificationText(GradeEntry entry) {
        StringBuilder sb = new StringBuilder("Нова оцінка в eCampus!\n\n");

        if (entry.getDisciplineName() != null) {
            sb.append("Предмет: ").append(entry.getDisciplineName()).append("\n");
        }
        if (entry.getMark() != null) {
            sb.append("Оцінка: ").append(entry.getMark()).append("\n");
        }
        if (entry.getPresence() != null && !entry.getPresence().isBlank()) {
            sb.append("Присутність: ").append(entry.getPresence()).append("\n");
        }
        if (entry.getDescription() != null) {
            sb.append("За що: ").append(entry.getDescription()).append("\n");
        }
        if (entry.getEmployeeFullName() != null) {
            sb.append("Викладач: ").append(entry.getEmployeeFullName());
        }

        return sb.toString().trim();
    }

}
