package dev.ua.ikeepcalm.lumios.web.endpoints;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import dev.ua.ikeepcalm.lumios.web.endpoints.campus.GradeEntry;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
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
                log.error("Failed to deliver grade notification for externalId={}: {}", entry.getId(), e.getMessage());
            }
        }

        return ResponseEntity.ok().build();
    }

    private void deliverGradeNotification(GradeEntry entry) {
        if (entry.getId() == null || entry.getId().isBlank()) {
            log.warn("Received grade entry with no id, skipping");
            return;
        }

        CampusBinding binding;
        try {
            binding = campusBindingService.findByExternalId(entry.getId());
        } catch (NoSuchEntityException e) {
            log.debug("No campus binding found for externalId={}, skipping", entry.getId());
            return;
        }

        TextMessage message = new TextMessage();
        message.setChatId(binding.getTelegramUserId());
        message.setText(buildNotificationText(entry));
        message.setParseMode(ParseMode.MARKDOWN);
        telegramClient.sendTextMessage(message);
    }

    private String buildNotificationText(GradeEntry entry) {
        StringBuilder sb = new StringBuilder();

        boolean hasGrade = entry.getMark() != null;
        sb.append(hasGrade ? "📊 *Нова оцінка в eCampus*" : "📋 *Нова відмітка в eCampus*").append("\n\n");

        if (entry.getDisciplineName() != null) {
            sb.append("📚 ").append(entry.getDisciplineName()).append("\n");
        }
        if (entry.getDescription() != null) {
            sb.append("📝 ").append(entry.getDescription()).append("\n");
        }
        if (hasGrade) {
            sb.append("⭐ Оцінка: *").append(entry.getMark()).append("*\n");
        }
        if (entry.getPresence() != null && !entry.getPresence().isBlank()) {
            sb.append("✅ Присутність: ").append(entry.getPresence()).append("\n");
        }
        if (entry.getEmployeeFullName() != null) {
            sb.append("👨‍🏫 ").append(entry.getEmployeeFullName());
        }

        return sb.toString().trim();
    }

}
