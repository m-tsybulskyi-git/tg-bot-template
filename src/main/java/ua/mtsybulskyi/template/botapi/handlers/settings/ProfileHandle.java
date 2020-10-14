package ua.mtsybulskyi.template.botapi.handlers.settings;

import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProfileHandle extends InputHandler {

    protected ProfileHandle(LocaleMessageService messageService,
                            UserDataService userDataService,
                            @Lazy HandlerService handlerService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        localeTag = userDataService.getLanguageTag(message.getChatId());
        long chatId = message.getChatId();
        return getReplyMessage(message, getUserData(chatId), false, true, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Map<String, BotState> map = Map.of(
                "profileEdit", BotState.PROFILE_EDIT);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    public BotState getPreviousHandler() {
        return BotState.MENU_SETTINGS;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SETTINGS_PROFILE;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(messageService.getMessage("profile.edit", localeTag));
        button1.setCallbackData("profileEdit");


        List<InlineKeyboardButton> row1 = List.of(button1);
        return List.of(row1, getBackButton());

    }

    private String getUserData(long chatId){
        UserData user = userDataService.getUserProfileData(chatId);
        String age = userDataService.getAge(chatId);

        String gender = switch (user.getGender()){
            case "woman" -> messageService.getMessage("woman", localeTag);
            case "man" -> messageService.getMessage("man", localeTag);
            default -> "\uD83D\uDEAB";
        };

        String userInfo = "<u>" + messageService.getMessage("settings.profile", localeTag) + "</u>\n";
        userInfo += "<b>" + messageService.getMessage("profile.name", localeTag) + "</b>" +
                    " " + user.getName() + "\n";
        userInfo += "<b>" + messageService.getMessage("profile.surname", localeTag) + "</b>" +
                    " " + user.getSurname() +"\n";
        userInfo += "<b>" + messageService.getMessage("profile.email", localeTag) + "</b>" +
                    " " + user.getEmail() +"\n";
        userInfo += "<b>" + messageService.getMessage("profile.gender", localeTag) + "</b>" +
                    " " + gender+ "\n";
        userInfo += "<b>" + messageService.getMessage("profile.age", localeTag) + "</b>" +
                    " " + age + "\n";
        return userInfo;
    }
}