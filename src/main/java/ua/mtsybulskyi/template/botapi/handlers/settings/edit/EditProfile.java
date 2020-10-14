package ua.mtsybulskyi.template.botapi.handlers.settings.edit;

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

        if (userDataService.getUserState(chatId).equals(BotState.PROFILE_EDIT)) {
            userDataService.setUserState(chatId, BotState.PROFILE_NAME);
            userDataService.setLastMessageFromBot(chatId, message);
        }
        Message botMessage = userDataService.getLastMessageFromBot(chatId);
        return processUsersInput(message, botMessage);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String text = callbackQuery.getData();
        Message messageFromBot = userDataService.getLastMessageFromBot(chatId);

        BotState botState = userDataService.getUserState(chatId);
        if (botState.equals(BotState.PROFILE_AGE) && !text.equals("back") && !text.equals("error")) {
            userDataService.setUserState(chatId, BotState.PROFILE_FILLED);
            if(!text.equals("next")) userDataService.setGender(chatId, text);

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
        BotState botState = userDataService.getUserState(chatId);
        log.info(botState.toString());

        boolean nextState = message.getMessageId().equals(botMessage.getMessageId());

        if (botState.equals(BotState.PROFILE_NAME)) {
            userDataService.setUserState(chatId, BotState.PROFILE_SURNAME);
            return getReplyMessage(botMessage, "profile.name", false, null);
        }

        if (botState.equals(BotState.PROFILE_SURNAME)) {
            if (!nextState) userDataService.setName(chatId, usersAnswer);
            userDataService.setUserState(chatId, BotState.PROFILE_EMAIL);
            return getReplyMessage(botMessage, "profile.surname", false, null);
        }

        if (botState.equals(BotState.PROFILE_EMAIL)) {
            if (!nextState) userDataService.setSurname(chatId, usersAnswer);
            userDataService.setUserState(chatId, BotState.PROFILE_GENDER);
            return getReplyMessage(botMessage, "profile.email", false, null);
        }

        if (botState.equals(BotState.PROFILE_GENDER)) {
            if (!nextState) {
                if (emailValidation(message.getText())) {
                    return getReplyMessage(botMessage, "profile.email", false, "error.profile.email");
                }
                userDataService.setEmail(chatId, usersAnswer);
            }

            userDataService.setUserState(chatId, BotState.PROFILE_AGE);
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
                    userDataService.setAge(chatId, age);
                } catch (NumberFormatException exception) {
                    return getReplyMessage(botMessage, "profile.age", false, "error.profile.age");
                }

                userDataService.setUserState(chatId, BotState.SETTINGS_PROFILE);
                return redirectFromMessage(botMessage, BotState.SETTINGS_PROFILE);
            }
        }

        return null;
    }


    @Override
    public BotState getPreviousHandler() {
        return BotState.SETTINGS_PROFILE;
    }

    @Override
    protected List<List<InlineKeyboardButton>> getKeyboard() {
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
