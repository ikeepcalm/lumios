package dev.ua.ikeepcalm.lumios.telegram.core.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {
    String command() default "";

    String startsWith() default "";

    String[] aliases() default {};

    boolean isPrivate() default false;
}
