package dev.ua.ikeepcalm.lumios.telegram.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TaskService {

    @Async
    @Transactional
    public CompletableFuture<Void> processChatsInBatches(Runnable taskLogic, String taskName) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting {} task", taskName);
                taskLogic.run();
                log.info("Completed {} task successfully", taskName);
            } catch (Exception e) {
                log.error("Error executing {} task", taskName, e);
                throw new RuntimeException("Task failed: " + taskName, e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> processWithRetry(Runnable task, String taskName, int maxRetries) {
        return CompletableFuture.runAsync(() -> {
            int attempts = 0;
            Exception lastException = null;
            
            while (attempts < maxRetries) {
                try {
                    task.run();
                    return;
                } catch (Exception e) {
                    lastException = e;
                    attempts++;
                    log.warn("Attempt {} failed for task {}: {}", attempts, taskName, e.getMessage());
                    
                    if (attempts < maxRetries) {
                        try {
                            Thread.sleep(1000L * attempts);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Task interrupted: " + taskName, ie);
                        }
                    }
                }
            }
            
            throw new RuntimeException("Task failed after " + maxRetries + " attempts: " + taskName, lastException);
        });
    }
}