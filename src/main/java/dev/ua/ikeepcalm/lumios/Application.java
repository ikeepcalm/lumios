package dev.ua.ikeepcalm.lumios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a command (type 'stop' to exit): ");
        label:
        while (true) {
            if (scanner.hasNext()) {
                String input = scanner.nextLine();

                String[] parts = input.split(" ", 2);
                String command = parts[0].toLowerCase();
                String arguments = parts.length > 1 ? parts[1] : null;

                switch (command) {
                    case "stop":
                        logger.info("Shutting down the application...");
                        SpringApplication.exit(applicationContext, () -> 0);
                        break label;
                    case "restart":
                        logger.info("Restarting the application...");
                        SpringApplication.exit(applicationContext, () -> 0);
                        SpringApplication.run(Application.class, arguments);
                        break label;
                    default:
                        logger.info("Unknown command. Please, try again.");
                        break;
                }
            }
        }
        scanner.close();
    }
}
