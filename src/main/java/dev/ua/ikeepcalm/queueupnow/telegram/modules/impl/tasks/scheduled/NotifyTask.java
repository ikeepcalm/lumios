package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.tasks.scheduled;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.TaskService;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import dev.ua.ikeepcalm.queueupnow.telegram.TelegramClient;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Scheduled(cron = "0 0 6 * * *")
    public void notifyAboutTasks() {
        Iterable<ReverenceChat> chats = chatService.findAll();
        for (ReverenceChat chat : chats) {
            List<DueTask> tasks = taskService.getTasksForCurrentChat(chat);
            for (DueTask task : tasks) {
                if (task.getDueDate().isBefore(task.getDueDate().minusDays(3))) {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setChatId(chat.getChatId());
                    textMessage.setText(String.format("""
                            Нагадуємо, що у вас є завдання, яке має бути виконане найближчим часом:
                            %s
                            Дедлайн: %s
                            """, task.getTaskName(), task.getDueDate()));
                    telegramClient.sendTextMessage(textMessage);
                }
            }
        }
    }
}