package ua.mtsybulskyi.template.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocaleMessageService {
    private final MessageSource messageSource;

    public LocaleMessageService( MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String message, String localeTag) {
        Locale locale = Locale.forLanguageTag(localeTag);
        return messageSource.getMessage(message, null, locale);
    }

    public String getMessage(String message, String localeTag,  Object... args) {
        Locale locale = Locale.forLanguageTag(localeTag);
        return messageSource.getMessage(message, args, locale);
    }

}
