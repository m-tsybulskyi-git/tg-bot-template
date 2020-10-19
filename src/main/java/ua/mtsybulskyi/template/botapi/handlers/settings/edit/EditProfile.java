package ua.mtsybulskyi.template.botapi.handlers.settings.edit;

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
import java.util.Map;

@Component
public class EditProfile extends InputHandler {

    protected EditProfile(LocaleMessageService messageService,
                          UserDataService userDataService,
                          @Lazy HandlerService handlerService) {
        super(messageService, userDataService, handlerService);
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        localeTag = userDataService.getLanguageTag(chatId);
        UserData user = userDataService.getUserData(chatId);

        if (userDataService.getUserState(chatId).equals(BotState.PROFILE_EDIT)) {
            user.setBotState(BotState.PROFILE_NAME);
            user.setMessage(message);
        }

        Message botMessage = user.getMessage();
        return processUsersInput(message, botMessage);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        UserData user = userDataService.getUserData(chatId);
        String data = callbackQuery.getData();
        Message messageFromBot = user.getMessage();

        BotState botState = userDataService.getUserState(chatId);
        if (botState.equals(BotState.PROFILE_AGE) && !data.equals("back") && !data.equals("error")) {
            user.setBotState(BotState.PROFILE_FILLED);
            if(!data.equals("next")) user.setGender(data);

            return getReplyMessage(messageFromBot, "profile.age", false, null);
        }

        return redirectFromCallback(callbackQuery, Map.of("next", botState));
    }

    @Override
    public BotState getHandlerName() {
        return BotState.PROFILE_EDIT;
    }

    private BotApiMethod<?> processUsersInput(Message message, Message botMessage) {
        String usersAnswer = message.getText();
        long chatId = message.getChatId();
        UserData user = userDataService.getUserData(chatId);
        BotState botState = user.getBotState();
        log.info(botState.toString());

        boolean nextState = message.getMessageId().equals(botMessage.getMessageId());

        if (botState.equals(BotState.PROFILE_NAME)) {
            user.setBotState(BotState.PROFILE_SURNAME);
            return getReplyMessage(botMessage, "profile.first_name", false, null);
        }

        if (botState.equals(BotState.PROFILE_SURNAME)) {
            if (!nextState) user.setFirstName(usersAnswer);
            user.setBotState(BotState.PROFILE_EMAIL);
            return getReplyMessage(botMessage, "profile.last_name", false, null);
        }

        if (botState.equals(BotState.PROFILE_EMAIL)) {
            if (!nextState) user.setLastName(usersAnswer);
            user.setBotState(BotState.PROFILE_GENDER);
            return getReplyMessage(botMessage, "profile.email", false, null);
        }

        if (botState.equals(BotState.PROFILE_GENDER)) {
            if (!nextState) {
                if (emailValidation(message.getText())) {
                    return getReplyMessage(botMessage, "profile.email", false, "error.profile.email");
                }
                user.setEmail(usersAnswer);
            }

            user.setBotState(BotState.PROFILE_AGE);
            return getReplyMessage(botMessage, "profile.gender", false, getGenderKeyboard(), null);
        }

        if(botState.equals(BotState.PROFILE_AGE)){
            if (!nextState) {
                return getReplyMessage(botMessage, "profile.gender", false, getGenderKeyboard(), "error.profile.gender");
            }
        }

        if (botState.equals(BotState.PROFILE_FILLED)) {
            int age;
            if (!nextState) {
                try {
                    age = Integer.parseInt(usersAnswer);
                    user.setAge(age);
                } catch (NumberFormatException exception) {
                    return getReplyMessage(botMessage, "profile.age", false, "error.profile.age");
                }
            }
            user.setBotState(BotState.SETTINGS_PROFILE);
            return redirectFromMessage(botMessage, BotState.SETTINGS_PROFILE);
        }

        return null;
    }


    @Override
    public BotState getPreviousHandlerName() {
        return BotState.SETTINGS_PROFILE;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard(long chatId) {
        return List.of(getInlineNavigation());
    }

    List<InlineKeyboardButton> getInlineNavigation() {
        InlineKeyboardButton nextButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.continue", localeTag));
        nextButton.setCallbackData("next");
        List<InlineKeyboardButton> navigationRow = new java.util.ArrayList<>(List.copyOf(getBackButton()));
        navigationRow.add(nextButton);
        return navigationRow;
    }

    private List<List<InlineKeyboardButton>> getGenderKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton()
                .setText(messageService.getMessage("man", localeTag));
        button1.setCallbackData("man");

        InlineKeyboardButton button2 = new InlineKeyboardButton()
                .setText(messageService.getMessage("woman", localeTag));
        button2.setCallbackData("woman");

        List<InlineKeyboardButton> row1 = List.of(button1, button2);
        List<List<InlineKeyboardButton>> keyboard = List.of(row1, getInlineNavigation());

        return keyboard;
    }

    boolean emailValidation(String email) {
        return !email.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b");
    }
}
