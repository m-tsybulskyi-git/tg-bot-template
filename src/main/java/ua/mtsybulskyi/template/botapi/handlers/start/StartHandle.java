package ua.mtsybulskyi.template.botapi.handlers.start;

import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StartHandle extends InputHandler {
    private String localeTag;

    public StartHandle(@Lazy HandlerService handlerService,
                       LocaleMessageService messageService,
                       UserDataService userDataService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        userDataService.saveStartUserData(message);
        localeTag = userDataService.getLanguageTag(message.getChatId());
        return getReplyMessage(message, getMessageText(message), null, true, null);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        userDataService.saveStartUserData(callbackQuery.getMessage());
        Map<String, BotState> map = Map.of("continue", BotState.MENU_MAIN,
                                            "language", BotState.SETTINGS_LANGUAGE);
        return redirectFromCallback(callbackQuery, map);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.START;
    }

    @Override
    public BotState getPreviousHandlerName() {
        return getHandlerName();
    }

    @Override
    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId){
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(messageService.getMessage("settings.language", localeTag));
        button1.setCallbackData("language");

        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.continue", localeTag));
        button2.setCallbackData("continue");
        List<InlineKeyboardButton> row1 = List.of(button1, button2);

        return List.of(row1);
    }

    private String getMessageText(Message message){
        long chatId = message.getChatId();
        return messageService
                .getMessage("message.start", localeTag,
                        userDataService.getFirstName(chatId),
                        userDataService.getLastName(chatId));
    }
}