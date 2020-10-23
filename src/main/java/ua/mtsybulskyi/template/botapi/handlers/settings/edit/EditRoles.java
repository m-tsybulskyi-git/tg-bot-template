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
import ua.mtsybulskyi.template.service.Emoji;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.ArrayList;
import java.util.List;

@Component
public class EditRoles extends InputHandler {
    private UserData user;
    private final DataCache dataCache;

    public BotState getHandlerName() {
        return BotState.ROLES_EDIT;
    }

    public BotState getPreviousHandlerName() {
        return BotState.SETTINGS_CHANGE_ROLES;
    }


    protected EditRoles(LocaleMessageService messageService,
                        UserDataService userDataService,
                        @Lazy HandlerService handlerService,
                        DataCache dataCache) {
        super(messageService, userDataService, handlerService);
        this.dataCache = dataCache;
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        languageTag = userDataService.getLanguageTag(message.getChatId());
        user = dataCache.getUserData(message.getChatId());

        if (user == null) return redirectFromMessage(message, getPreviousHandlerName());

        return getReplyMessage(message, getUserShortInfo(user),
                null, false, null);
    }

    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        languageTag = userDataService.getLanguageTag(chatId);
        user = dataCache.getUserData(chatId);

        if (user == null) return redirectFromMessage(
                callbackQuery.getMessage(), getPreviousHandlerName());

        String error = null;

        switch (callbackQuery.getData()) {
            case "back" -> {
                BotState botState = getPreviousHandlerName();
                userDataService.setBotState(chatId, botState);
                return redirectFromMessage(callbackQuery.getMessage(), botState);
            }
            case "error" -> error = "error.roles";
            default -> userDataService.setRole(user.getChatId(), callbackQuery.getData());
        }

        return getReplyMessage(callbackQuery.getMessage(), getUserShortInfo(user),
                null, false, error);
    }

    private String getUserShortInfo(UserData user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (userDataService.hasPrivilege(chatId, "CHANGE_ROLES_PRIVILEGE")) {
            userDataService.getRoles(true).forEach(x -> keyboard.add(getRoleButton(chatId, x)));
        }

        keyboard.add(getBackButton());

        return keyboard;
    }

    private List<InlineKeyboardButton> getRoleButton(long chatId, Role role) {
        InlineKeyboardButton userButton = new InlineKeyboardButton();
        String text = messageService.getMessage(role.getName(), languageTag);
        String userRole = userDataService.getUserRoleString(user.getChatId());
        String currentRole = role.getName();

        if (currentRole.equals(userRole)) text += " " + Emoji.CURRENT.toString();
        else if (currentRole.equals(Roles.ADMIN_ROLE.toString())) text += " " + Emoji.ADMIN.toString();
        else if (currentRole.equals(Roles.WORKER_ROLE.toString())) text += " " + Emoji.WORKER.toString();


        if (Roles.valueOf(userDataService.getUserRoleString(chatId)).getPriority() > Roles.valueOf(role.getName()).getPriority()) {
            text += " \uD83D\uDEAB";
            userButton.setCallbackData("error");
        } else {
            userButton.setCallbackData(role.getName());
        }

        userButton.setText(text);
        return List.of(userButton);
    }
}
