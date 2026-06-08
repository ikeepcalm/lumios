package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Configuration
public class TranslationService {

    private final MessageSource messageSource;

    public TranslationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, LumiosChat chat, Object... args) {
        String lang = (chat != null) ? chat.getLanguage() : "uk";
        return messageSource.getMessage(key, args, Locale.forLanguageTag(lang));
    }

    public String getMessage(String key, String lang, Object... args) {
        if (lang == null) {
            lang = "uk";
        }
        return messageSource.getMessage(key, args, Locale.forLanguageTag(lang));
    }

    @Bean
    public static MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.forLanguageTag("uk"));
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
}
