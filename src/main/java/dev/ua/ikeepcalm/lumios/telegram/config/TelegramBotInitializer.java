package dev.ua.ikeepcalm.lumios.telegram.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Objects;

@Component
public class TelegramBotInitializer implements InitializingBean {
    private final TelegramBotsLongPollingApplication telegramBotsApi;
    private final List<LongPollingSingleThreadUpdateConsumer> longPollingBots;
    private final Environment environment;

    public TelegramBotInitializer(TelegramBotsLongPollingApplication telegramBotsApi,
                                  List<LongPollingSingleThreadUpdateConsumer> longPollingBots, Environment environment) {
        this.environment = environment;
        Objects.requireNonNull(telegramBotsApi);
        Objects.requireNonNull(longPollingBots);
        this.telegramBotsApi = telegramBotsApi;
        this.longPollingBots = longPollingBots;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            for (LongPollingSingleThreadUpdateConsumer bot : longPollingBots) {
                telegramBotsApi.registerBot(environment.getProperty("TELEGRAM_API_KEY"), () -> TelegramUrl.DEFAULT_URL, new DefaultGetUpdatesGenerator(List.of("channel_post", "inline_query", "message", "callback_query", "message_reaction", "chat_member")), bot);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}