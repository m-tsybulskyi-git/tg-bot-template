package ua.mtsybulskyi.template.botapi.handlers.settings;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.List;

@Component
public class LanguageHandler extends InputHandler {

    public LanguageHandler(LocaleMessageService messageService,
                           UserDataService userDataService,
                           @Lazy HandlerService handlerService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        UserData user = userDataService.getUserData(chatId);

        if (!user.getMessage().getMessageId().equals(message.getMessageId())) {
            return getReplyMessage(user.getMessage(),
                    "settings.language", false, "error.language");
        }

        return getReplyMessage(message, "settings.language", false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserData user =  userDataService.getUserData(chatId);
        String error = null;

        switch (callbackQuery.getData()) {
            case "locale.eu" -> {
                localeTag = "eu-EU";
               user.setLanguage("eu-EU");
            }
            case "locale.ua" -> {
                localeTag = "ua-UA";
                user.setLanguage("ua-UA");
            }
            case "locale.ru" -> {
                localeTag = "ru-RU";
                user.setLanguage("ru-RU");
            }
            case "back" -> {
                BotState botState = getPreviousHandlerName();
                user.setBotState(botState);
                return handlerService.getHandler(botState).handle(callbackQuery.getMessage());
            }

            case "error" -> error = "error.language";
        }

        return getReplyMessage(callbackQuery.getMessage(), "settings.language", false, error);
    }

    @Override
    public BotState getPreviousHandlerName() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SETTINGS_LANGUAGE;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard(long chatId) {

        InlineKeyboardButton enButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.eu", localeTag)
        );
        enButton.setCallbackData("locale.eu");

        InlineKeyboardButton uaButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.ua", localeTag)
        );
        uaButton.setCallbackData("locale.ua");

        InlineKeyboardButton ruButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.ru", localeTag)
        );
        ruButton.setCallbackData("locale.ru");

        List<InlineKeyboardButton> languages = List.of(enButton, uaButton, ruButton);
        return List.of(languages, getBackButton());
    }
}
