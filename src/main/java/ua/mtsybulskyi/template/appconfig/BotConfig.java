package ua.mtsybulskyi.template.appconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ua.mtsybulskyi.template.QuestionAnsweringBot;
import ua.mtsybulskyi.template.botapi.TelegramFacade;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "bot.settings")
public class BotConfig implements WebMvcConfigurer {
    private String webHookPath;
    private String botUserName;
    private String botToken;

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/resources/",
            "classpath:/static/docs/",
            "classpath:messages/messages" };

    @Bean
    public QuestionAnsweringBot myWizardTelegramBot(TelegramFacade telegramFacade) {

        QuestionAnsweringBot questionAnsweringBot = new QuestionAnsweringBot(telegramFacade);
        questionAnsweringBot.setBotUsername(botUserName);
        questionAnsweringBot.setBotToken(botToken);
        questionAnsweringBot.setBotPath(webHookPath);

        return questionAnsweringBot;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasenames(CLASSPATH_RESOURCE_LOCATIONS);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
