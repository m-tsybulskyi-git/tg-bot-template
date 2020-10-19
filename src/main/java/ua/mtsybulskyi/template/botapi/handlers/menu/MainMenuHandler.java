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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;

@Component
public class MainMenuHandler extends InputHandler {

    protected MainMenuHandler(LocaleMessageService messageService,
                              UserDataService userDataService,
                              @Lazy HandlerService handlerService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        localeTag = userDataService.getLanguageTag(chatId);
        return getReplyMessage(message, "menu.main", false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Map<String, BotState> map = Map.of(
                "settings", BotState.MENU_SETTINGS);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.MENU_MAIN;
    }

    @Override
    public BotState getPreviousHandlerName() {
        return getHandlerName();
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard(long chatId) {
        InlineKeyboardButton settings = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.settings", localeTag));
        settings.setCallbackData("settings");
        List<InlineKeyboardButton> settingsRow = List.of(settings);

        List<List<InlineKeyboardButton>> keyboard = List.of(settingsRow);
        return keyboard;
    }
}
