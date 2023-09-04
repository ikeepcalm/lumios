package dev.ua.ikeepcalm.queueupnow.telegram.executing;

import dev.ua.ikeepcalm.queueupnow.telegram.servicing.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class Executable{

    protected TelegramService telegramService;
    protected Logger logger;

    @Autowired
    private void init(TelegramService telegramService){
        this.telegramService = telegramService;
        this.logger = LoggerFactory.getLogger(SLF4JServiceProvider.class);
    }
}
