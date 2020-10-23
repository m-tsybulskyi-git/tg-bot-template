package ua.mtsybulskyi.template.botapi.handlers.settings;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.List;
import java.util.Map;

@Component
public class ProfileHandle extends InputHandler {

    protected ProfileHandle(LocaleMessageService messageService,
                            UserDataService userDataService,
                            @Lazy HandlerService handlerService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        languageTag = userDataService.getLanguageTag(chatId);

        return getReplyMessage(message, getUserData(chatId), null, false, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Map<String, BotState> map = Map.of(
                "profileEdit", BotState.PROFILE_EDIT);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    public BotState getPreviousHandlerName() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SETTINGS_PROFILE;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId) {
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(messageService.getMessage("profile.edit", languageTag));
        button1.setCallbackData("profileEdit");


        List<InlineKeyboardButton> row1 = List.of(button1);
        return List.of(row1, getBackButton());
    }

    private String getUserData(long chatId) {
        String gender = userDataService.getGender(chatId);
        gender = switch (gender) {
            case "woman", "man" -> messageService.getMessage(gender, languageTag);
            default -> "\uD83D\uDEAB";
        };

        String userInfo = "<u>" + messageService.getMessage("settings.profile", languageTag) + "</u>\n";
        userInfo += "<b>" + messageService.getMessage("profile.first_name", languageTag) + "</b>" +
                " " + userDataService.getFirstName(chatId) + "\n";
        userInfo += "<b>" + messageService.getMessage("profile.last_name", languageTag) + "</b>" +
                " " + userDataService.getLastName(chatId) + "\n";
        userInfo += "<b>" + messageService.getMessage("profile.email", languageTag) + "</b>" +
                " " + userDataService.getEmail(chatId) + "\n";
        userInfo += "<b>" + messageService.getMessage("profile.gender", languageTag) + "</b>" +
                " " + gender + "\n";
        userInfo += "<b>" + messageService.getMessage("profile.age", languageTag) + "</b>" +
                " " + userDataService.getAge(chatId) + "\n";

        userInfo += "<b>" + messageService.getMessage("profile.role", languageTag) + "</b>" +
                " " + messageService.getMessage(userDataService.getUserRoleString(chatId), languageTag) + "\n";
        return userInfo;
    }
}