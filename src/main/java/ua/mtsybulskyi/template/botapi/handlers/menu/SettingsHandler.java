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
public class SettingsHandler extends InputHandler {

    public SettingsHandler(@Lazy HandlerService handlerService,
                           LocaleMessageService messageService,
                           UserDataService userDataService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    public BotState getPreviousHandlerName() {
        return BotState.MENU_MAIN;
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        languageTag = userDataService.getLanguageTag(message.getChatId());

        return getReplyMessage(message, "menu.settings", null, false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Map<String, BotState> map = Map.of(
                "profile", BotState.SETTINGS_PROFILE,
                "language", BotState.SETTINGS_LANGUAGE,
                "roles", BotState.SETTINGS_CHANGE_ROLES);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId) {

        InlineKeyboardButton profileButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("settings.profile", languageTag));
        profileButton.setCallbackData("profile");
        List<InlineKeyboardButton> profileRow = List.of(profileButton);


        InlineKeyboardButton languageButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("settings.language", languageTag));
        languageButton.setCallbackData("language");
        List<InlineKeyboardButton> languageRow = List.of(languageButton);

        List<List<InlineKeyboardButton>> keyboard = new java.util.ArrayList<>(List.of(profileRow, languageRow));

        if(userDataService.hasPrivilege(chatId, "CHANGE_ROLES_PRIVILEGE")) {
            InlineKeyboardButton rolesButton = new InlineKeyboardButton()
                    .setText(messageService.getMessage("settings.roles", languageTag));
            rolesButton.setCallbackData("roles");
            List<InlineKeyboardButton> rolesRow = List.of(rolesButton);
            keyboard.add(rolesRow);
        }

        keyboard.add(getBackButton());

        return keyboard;
    }
}
