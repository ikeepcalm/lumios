
package dev.ua.ikeepcalm.merged;


import dev.ua.ikeepcalm.merged.telegram.utils.InteractiveRunnerUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages={"dev.ua.ikeepcalm.merged", "org.telegram.telegrambots"})
public class Application implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final QueueLifecycleUtil queueLifecycleUtil;
    private final InteractiveRunnerUtil interactiveRunnerUtil;
    private final Logger logger;

    public Application(ApplicationContext applicationContext,
                       QueueLifecycleUtil queueLifecycleUtil,
                       InteractiveRunnerUtil interactiveRunnerUtil) {
        this.applicationContext = applicationContext;
        this.queueLifecycleUtil = queueLifecycleUtil;
        this.interactiveRunnerUtil = interactiveRunnerUtil;
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        label:
        while (true) {
            System.out.println("Enter a command (type 'stop' to exit): ");
            String input = scanner.nextLine();

            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();
            String arguments = parts.length > 1 ? parts[1] : null;

            switch (command) {
                case "stop":
                    logger.info("Shutting down the application...");
                    queueLifecycleUtil.saveHashMapToFile();
                    SpringApplication.exit(applicationContext);
                    System.exit(0);
                    break label;
                case "say":
                    if (arguments != null && !arguments.isEmpty()) {
                        logger.info("Executing TextMessage...");
                        interactiveRunnerUtil.ip32Command(arguments);
                    } else {
                        logger.info("You should also set desired text to be sent!");
                    }
                    break;
                case "announce":
                    if (arguments != null && !arguments.isEmpty()) {
                        logger.info("Executing TextMessage...");
                        interactiveRunnerUtil.announceCommand(arguments);
                    } else {
                        logger.info("You should also set desired text to be sent!");
                    }
                    break;
                default:
                    logger.info("Unknown command. Please, try again.");
                    break;
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
