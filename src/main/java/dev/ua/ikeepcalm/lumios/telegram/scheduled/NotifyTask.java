package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class NotifyTask {

    private final TaskService taskService;
    private final ChatService chatService;
    private final TelegramClient telegramClient;

    public NotifyTask(TaskService taskService, ChatService chatService, TelegramClient telegramClient) {
        this.taskService = taskService;
        this.chatService = chatService;
        this.telegramClient = telegramClient;
    }

    @Async
    @Scheduled(cron = "0 0 6 * * *")
    public void notifyAboutTasks() {
        try {
            log.info("Starting daily task notification");
            int notificationsSent = 0;
        Iterable<LumiosChat> chats = chatService.findAll();
        for (LumiosChat chat : chats) {
            List<DueTask> tasks = taskService.getTasksForCurrentChat(chat);
            for (DueTask task : tasks) {
                if (task.getDueDate().isAfter(LocalDate.now()) && task.getDueDate().isBefore(LocalDate.now().plusDays(3))) {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setChatId(chat.getChatId());
                    textMessage.setText(String.format("""
                            Нагадуємо, що у вас є завдання, яке має бути виконане найближчим часом:
                            %s
                            Дедлайн: %s
                            """, task.getTaskName(), task.getDueDate()));
                    telegramClient.sendTextMessage(textMessage);
                    notificationsSent++;
                }
            }
        }
        log.info("Completed daily task notification, sent {} notifications", notificationsSent);
        } catch (Exception e) {
            log.error("Failed to send task notifications", e);
        }
    }
}