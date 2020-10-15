package ua.mtsybulskyi.template.appconfig;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.mtsybulskyi.template.QuestionAnsweringBot;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import ua.mtsybulskyi.template.botapi.TelegramFacade;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "bot.settings")

public class BotConfig {
    private String webHookPath;
    private String botUserName;
    private String botToken;

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

        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
