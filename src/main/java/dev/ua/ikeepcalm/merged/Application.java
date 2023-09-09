
package dev.ua.ikeepcalm.merged;


import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages={"dev.ua.ikeepcalm.merged", "org.telegram.telegrambots"})
public class Application {
    public static Scheduler quartzScheduler;

    public static void main(String[] args) {
        try {
            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
        }
        catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        SpringApplication.run(Application.class, (String[])args);
    }
}

