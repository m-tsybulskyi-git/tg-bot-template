package ua.mtsybulskyi.template.botapi.handlers.settings.edit;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.cache.DataCache;
import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.domain.Roles;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.ArrayList;
import java.util.List;

@Component
public class EditRoles extends InputHandler {
    private UserData user;
    private final DataCache dataCache;

    protected EditRoles(LocaleMessageService messageService, UserDataService userDataService, @Lazy HandlerService handlerService, DataCache dataCache) {
        super(messageService, userDataService, handlerService);
        this.dataCache = dataCache;
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        localeTag = userDataService.getLanguageTag(message.getChatId());
        user = dataCache.getUserData(message.getChatId());
        if(user == null) return redirectFromMessage(message, getPreviousHandlerName());
        String text = user.getFirstName() + " " + user.getLastName();
        return getReplyMessage(message, text, false, true, null);
    }

    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        localeTag = userDataService.getLanguageTag(chatId);
        user = dataCache.getUserData(chatId);
        if(user == null) return redirectFromMessage(
                callbackQuery.getMessage(), getPreviousHandlerName());

        String error = null;

        switch (callbackQuery.getData()) {
            case "back" -> {
                BotState botState = getPreviousHandlerName();
                userDataService.setBotState(chatId, botState);
                return redirectFromMessage(callbackQuery.getMessage(), botState);
            }
            case "error" -> error ="error.roles";
            default -> userDataService.setRole(user.getChatId(), callbackQuery.getData());
        }

        String text = user.getFirstName() + " " + user.getLastName();
        return getReplyMessage(callbackQuery.getMessage(), text,
                false, true, error);
    }

    public BotState getHandlerName() {
        return BotState.ROLES_EDIT;
    }

    public BotState getPreviousHandlerName() {
        return BotState.SETTINGS_CHANGE_ROLES;
    }

    protected List<List<InlineKeyboardButton>> getKeyboard(long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (userDataService.hasPrivilege(chatId, "CHANGE_ROLES_PRIVILEGE")) {
            userDataService.getRoles(true).forEach(x -> keyboard.add(getRoleButton(chatId, x)));
        }

        keyboard.add(getBackButton());

        return keyboard;
    }

    private List<InlineKeyboardButton> getRoleButton(long chatId, Role role) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        String text = messageService.getMessage(role.getName(), localeTag);

        if (userDataService.getUserRoleString(user.getChatId()).equals(role.getName())) {
            text += " ✅";
        }

        String userRole = userDataService.getUserRoleString(chatId);
        if (Roles.valueOf(userRole).getPriority() > Roles.valueOf(role.getName()).getPriority()) {
            text += " \uD83D\uDEAB";
            button.setCallbackData("error");
        }else{
            button.setCallbackData(role.getName());
        }

        button.setText(text);

        return List.of(button);
    }
}
