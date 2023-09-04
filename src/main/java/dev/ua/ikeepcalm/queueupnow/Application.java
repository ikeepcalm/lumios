package dev.ua.ikeepcalm.queueupnow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"dev.ua.ikeepcalm.queueupnow", "org.telegram.telegrambots"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}