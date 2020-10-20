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
                    "settings.language", null, false, "error.language");
        }

        return getReplyMessage(message, "settings.language", null, false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserData user =  userDataService.getUserData(chatId);
        String error = null;

        switch (callbackQuery.getData()) {
            case "language.eu" -> {
                languageTag = "eu-EU";
               user.setLanguage("eu-EU");
            }
            case "language.ua" -> {
                languageTag = "ua-UA";
                user.setLanguage("ua-UA");
            }
            case "language.ru" -> {
                languageTag = "ru-RU";
                user.setLanguage("ru-RU");
            }
            case "back" -> {
                BotState botState = getPreviousHandlerName();
                user.setBotState(botState);
                return handlerService.getHandler(botState).handle(callbackQuery.getMessage());
            }

            case "error" -> error = "error.language";
        }

        return getReplyMessage(callbackQuery.getMessage(), "settings.language", null, false, error);
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
    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId) {

        InlineKeyboardButton enButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.eu", languageTag)
        );
        enButton.setCallbackData("language.eu");

        InlineKeyboardButton uaButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.ua", languageTag)
        );
        uaButton.setCallbackData("language.ua");

        InlineKeyboardButton ruButton = new InlineKeyboardButton().setText(
                messageService.getMessage("language.ru", languageTag)
        );
        ruButton.setCallbackData("language.ru");

        List<InlineKeyboardButton> languages = List.of(enButton, uaButton, ruButton);
        return List.of(languages, getBackButton());
    }
}
