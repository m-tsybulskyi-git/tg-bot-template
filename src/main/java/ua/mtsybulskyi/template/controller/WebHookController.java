package ua.mtsybulskyi.template.controller;

import ua.mtsybulskyi.template.QuestionAnsweringBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebHookController {
    private final QuestionAnsweringBot telegramBot;

    public WebHookController(QuestionAnsweringBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
