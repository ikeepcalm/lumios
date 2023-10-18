
package dev.ua.ikeepcalm.merged;


import dev.ua.ikeepcalm.merged.telegram.utils.InteractiveRunnerUtil;
import dev.ua.ikeepcalm.merged.telegram.utils.QueueLifecycleUtil;
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

    public Application(ApplicationContext applicationContext,
                       QueueLifecycleUtil queueLifecycleUtil,
                       InteractiveRunnerUtil interactiveRunnerUtil) {
        this.applicationContext = applicationContext;
        this.queueLifecycleUtil = queueLifecycleUtil;
        this.interactiveRunnerUtil = interactiveRunnerUtil;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter a command (type 'stop' to exit): ");
            String input = scanner.nextLine();

            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();
            String arguments = parts.length > 1 ? parts[1] : null;

            if (command.equals("stop")) {
                System.out.println("Shutting down the application...");
                queueLifecycleUtil.saveHashMapToFile();
                SpringApplication.exit(applicationContext);
                System.exit(0);
                break;
            } else if (command.equals("say")){
                if (arguments != null){
                System.out.println("Executing TextMessage...");
                interactiveRunnerUtil.sayCommand(arguments);
                }else {
                    System.out.println("You should also set desired text to be sent!");
                }
            } else {
                System.out.println("Unknown command. Please, try again.");
            }
        } scanner.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
