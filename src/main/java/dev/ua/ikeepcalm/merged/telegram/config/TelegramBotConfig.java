package dev.ua.ikeepcalm.merged.telegram.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource(value={"classpath:thirdparty.properties"})
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
