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

import java.util.ArrayList;
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
    public BotState getHandlerName() {
        return BotState.PROFILE_EDIT;
    }

    @Override
    public BotState getPreviousHandlerName() {
        return BotState.SETTINGS_PROFILE;
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        languageTag = userDataService.getLanguageTag(chatId);
        UserData user = userDataService.getUserData(chatId);

        if (userDataService.getUserState(chatId).equals(BotState.PROFILE_EDIT))
            user.setBotState(BotState.PROFILE_NAME);  // 1 < use those numbers to understand profile filling logic

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

        if (botState.equals(BotState.PROFILE_AGE) &&
                !data.equals("back") &&
                !data.equals("error")) {
            user.setBotState(BotState.PROFILE_FILLED);

            if (!data.equals("next")) user.setGender(data);

            return getReplyMessage(messageFromBot, "profile.age", null, false, null);
        }

        return redirectFromCallback(callbackQuery, Map.of("next", botState));
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
            return getReplyMessage(botMessage, "profile.first_name",
                    null, false, null);
        }

        if (botState.equals(BotState.PROFILE_SURNAME)) {
            if (!nextState) user.setFirstName(usersAnswer);
            user.setBotState(BotState.PROFILE_EMAIL);
            return getReplyMessage(botMessage, "profile.last_name",
                    null, false, null);
        }

        if (botState.equals(BotState.PROFILE_EMAIL)) {
            if (!nextState) user.setLastName(usersAnswer);
            user.setBotState(BotState.PROFILE_GENDER);
            return getReplyMessage(botMessage, "profile.email",
                    null, false, null);
        }

        if (botState.equals(BotState.PROFILE_GENDER)) {
            if (!nextState) {
                if (!isEmail(message.getText()))
                    return getReplyMessage(botMessage, "profile.email",
                            null, false, "error.profile.email");
                user.setEmail(usersAnswer);
            }

            user.setBotState(BotState.PROFILE_AGE);
            return getReplyMessage(botMessage, "profile.gender", getGenderKeyboard(),
                    false, null);
        }

        if (botState.equals(BotState.PROFILE_AGE)) {
            if (!nextState) {
                return getReplyMessage(botMessage, "profile.gender", getGenderKeyboard(),
                        false, "error.profile.gender");
            }
        }

        if (botState.equals(BotState.PROFILE_FILLED)) {
            int age;
            if (!nextState) {
                try {
                    age = Integer.parseInt(usersAnswer);
                    user.setAge(age);
                } catch (NumberFormatException exception) {
                    return getReplyMessage(botMessage, "profile.age",
                            null, false, "error.profile.age");
                }
            }
            user.setBotState(BotState.SETTINGS_PROFILE); // 9
            return redirectFromMessage(botMessage, getPreviousHandlerName());
        }

        return null;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId) {
        return List.of(getInlineNavigation());
    }

    List<InlineKeyboardButton> getInlineNavigation() {
        InlineKeyboardButton nextButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.continue", languageTag));
        nextButton.setCallbackData("next");
        List<InlineKeyboardButton> navigationRow = new ArrayList<>(List.copyOf(getBackButton()));
        navigationRow.add(nextButton);
        return navigationRow;
    }

    private List<List<InlineKeyboardButton>> getGenderKeyboard() {
        InlineKeyboardButton manButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("man", languageTag));
        manButton.setCallbackData("man");

        InlineKeyboardButton wonanButton = new InlineKeyboardButton()
                .setText(messageService.getMessage("woman", languageTag));
        wonanButton.setCallbackData("woman");

        List<InlineKeyboardButton> genderRow = List.of(manButton, wonanButton);

        return List.of(genderRow, getInlineNavigation());
    }

    boolean isEmail(String email) {
        return email.matches("\\b[\\w.%-] + @[-.\\w] + \\.[A-Za-z]{2,4}\\b");
    }
}
