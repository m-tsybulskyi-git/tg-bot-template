package ua.mtsybulskyi.template.botapi.handlers.menu;

import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SettingsHandler extends InputHandler {

    public SettingsHandler(@Lazy HandlerService handlerService,
                           LocaleMessageService messageService,
                           UserDataService userDataService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        localeTag = userDataService.getLanguageTag(message.getChatId());
        return getReplyMessage(message, "menu.settings", false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Map<String, BotState> map = Map.of(
                "profile", BotState.SETTINGS_PROFILE,
                "language", BotState.SETTINGS_LANGUAGE);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    public BotState getPreviousHandler() {
        return BotState.MENU_MAIN;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard() {

        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(messageService.getMessage("settings.profile", localeTag));
        button1.setCallbackData("profile");
        List<InlineKeyboardButton> row1 = List.of(button1);


        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(messageService.getMessage("settings.language", localeTag));
        button2.setCallbackData("language");
        List<InlineKeyboardButton> row2 = List.of(button2);


        List<List<InlineKeyboardButton>> keyboard = List.of(row1, row2, getBackButton());
        return keyboard;
    }
}
