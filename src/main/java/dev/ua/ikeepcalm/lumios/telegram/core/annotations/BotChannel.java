package dev.ua.ikeepcalm.lumios.telegram.core.annotations;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Transactional
@Retention(RetentionPolicy.RUNTIME)
public @interface BotChannel {

}
