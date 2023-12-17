package dev.ua.ikeepcalm.queue.telegram.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    @NotNull
    @NotEmpty
    private String token;

    @Value("${telegram.bot.username}")
    @NotNull
    @NotEmpty
    private String username;

}
