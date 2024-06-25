package dev.ua.ikeepcalm.lumios.telegram.core.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface BotInlineQuery {
    String inlineQuery() default "";
}
