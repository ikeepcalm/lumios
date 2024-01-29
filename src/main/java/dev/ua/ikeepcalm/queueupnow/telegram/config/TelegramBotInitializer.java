package dev.ua.ikeepcalm.queueupnow.telegram.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Component
public class TelegramBotInitializer implements InitializingBean {
    private final TelegramBotsApi telegramBotsApi;
    private final List<LongPollingBot> longPollingBots;

    public TelegramBotInitializer(TelegramBotsApi telegramBotsApi,
                                  List<LongPollingBot> longPollingBots) {
        Objects.requireNonNull(telegramBotsApi);
        Objects.requireNonNull(longPollingBots);
        this.telegramBotsApi = telegramBotsApi;
        this.longPollingBots = longPollingBots;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            for (LongPollingBot bot : longPollingBots) {
                telegramBotsApi.registerBot(bot);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}