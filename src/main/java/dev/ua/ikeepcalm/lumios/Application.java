package dev.ua.ikeepcalm.lumios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"dev.ua.ikeepcalm.lumios", "org.telegram.telegrambots"})
public class Application implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final Logger logger;

    public Application(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.logger = LoggerFactory.getLogger(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        logger.info("Lumios bot started successfully.");
        logger.info("Application is running. Send SIGTERM or SIGINT to stop gracefully.");
    }
}
