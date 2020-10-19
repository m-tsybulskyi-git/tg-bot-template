package ua.mtsybulskyi.template.botapi.handlers.settings;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.cache.DataCache;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RolesHandler extends InputHandler {
    private final DataCache dataCache;

    protected RolesHandler(LocaleMessageService messageService, UserDataService userDataService, @Lazy HandlerService handlerService, DataCache dataCache) {
        super(messageService, userDataService, handlerService);
        this.dataCache = dataCache;
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        localeTag = userDataService.getLanguageTag(chatId);
        return getReplyMessage(message, "settings.roles", false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();

        try {
            UserData user = userDataService.getUserData(
                    Long.parseLong(callbackQuery.getData()));

            dataCache.putUserData(chatId, user);
            user.setBotState(BotState.ROLES_EDIT);
            return redirectFromMessage(callbackQuery.getMessage(), BotState.ROLES_EDIT);
        }catch (NumberFormatException ignored){ }

        return redirectFromCallback(callbackQuery, Map.of());
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SETTINGS_CHANGE_ROLES;
    }

    @Override
    public BotState getPreviousHandlerName() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard(long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        UserData userData = userDataService.getUserData(chatId);

        if (userDataService.hasPrivilege(chatId, "CHANGE_ROLES_PRIVILEGE")) {
            userDataService.getUsers().forEach(x -> {
                if(!x.equals(userData))
                keyboard.add(getUserButton(x));
            });
        }

        keyboard.add(getBackButton());
        return keyboard;
    }

    private List<InlineKeyboardButton> getUserButton(UserData user) {
        InlineKeyboardButton userButton = new InlineKeyboardButton();
        String text = user.getFirstName() + " " + user.getLastName();
        if(userDataService.getUserRole(user.getChatId()).equals("ADMIN_ROLE")){
            text += " ⚜️";
        }

        userButton.setText(text);
        userButton.setCallbackData(String.valueOf(user.getChatId()));
        return List.of(userButton);
    }
}
