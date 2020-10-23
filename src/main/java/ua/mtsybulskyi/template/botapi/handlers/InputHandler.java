package ua.mtsybulskyi.template.botapi.handlers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.context.NoSuchMessageException;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.LocaleMessageService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.util.*;

/**
 * @author Mykyta Tsybulskyi
 * @version 1.0
 */

@RequiredArgsConstructor
public abstract class InputHandler {
    protected static final Logger log = org.slf4j.LoggerFactory.getLogger(InputHandler.class);

    public final LocaleMessageService messageService;
    public final UserDataService userDataService;
    public final HandlerService handlerService;

    private final String markdown = ParseMode.HTML;
    public String languageTag;


    public abstract BotState getHandlerName();

    public abstract BotState getPreviousHandlerName();

    public abstract BotApiMethod<?> handle(Message message);

    public abstract BotApiMethod<?> handle(CallbackQuery callbackQuery);


    /***
     * @param inputMessage message from user
     * @param textTag tag for text from resource messages
     * @param createMessage SendMessage = true;
     *                   EditMessage = false;
     * @param errorTag (Optional) tag for text from resource with error prefix
     * @return BotApiMethod (SendMessage, EditMessage ...)
     */
    protected BotApiMethod<?> getReplyMessage(Message inputMessage,
                                              String textTag,
                                              List<List<InlineKeyboardButton>> customKeyboard,
                                              boolean createMessage,
                                              String errorTag) {

        long chatId = inputMessage.getChatId();
        languageTag = userDataService.getLanguageTag(inputMessage.getChatId());

        if (textTag == null || textTag.isEmpty())    // < back to previous bot state if text tag is empty
            return redirectFromMessage(inputMessage, getPreviousHandlerName());

        String text;
        try {
            text = messageService.getMessage(textTag, languageTag);
        } catch (NoSuchMessageException e) {
            text = textTag;
        }

        InlineKeyboardMarkup keyboard;
        if (customKeyboard == null)
            keyboard = getInlineKeyboard(chatId, errorTag); // < default keyboard
        else keyboard = getInlineKeyboard(customKeyboard, errorTag);

        if (createMessage) return getSendMessage(chatId, text, keyboard);
        else return getEditMessage(chatId, text, keyboard, inputMessage);
    }

    protected BotApiMethod<?> redirectFromCallback(CallbackQuery callbackQuery, Map<String, BotState> map) {
        Map<String, BotState> stateMap = new HashMap<>(Map.copyOf(map));
        long chatId = callbackQuery.getMessage().getChatId();
        UserData userData = userDataService.getUserData(chatId);

        userData.setMessage(callbackQuery.getMessage()); // < set message for edit after bot restart

        stateMap.put("back", getPreviousHandlerName());  // < default back callback query

        BotState botState = stateMap.get(callbackQuery.getData());
        if (botState == null) return null; // < do not process unknown callback query's

        Message message = callbackQuery.getMessage();
        return redirectFromMessage(message, botState);
    }

    protected BotApiMethod<?> redirectFromMessage(Message message, BotState botState) {
        long chatId = message.getChatId();
        userDataService.setBotState(chatId, botState);
        return handlerService.findMessageHandler(botState).handle(message);
    }

    abstract protected List<List<InlineKeyboardButton>> getDefaultKeyboard(long chatId);

    protected InlineKeyboardMarkup getInlineKeyboard(long chatId, String error) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> errorRow = getErrorButton(error);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(
                List.copyOf(getDefaultKeyboard(chatId)));

        if (errorRow != null) keyboard.add(errorRow);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup getInlineKeyboard(List<List<InlineKeyboardButton>> customKeyboard, String error) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> errorRow = getErrorButton(error);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(
                List.copyOf(customKeyboard));

        if (errorRow != null) keyboard.add(errorRow);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * @return row of keyboard which contain back button
     */
    protected List<InlineKeyboardButton> getBackButton() {
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.back", languageTag));
        button.setCallbackData("back");

        return List.of(button);
    }

    /***
     * @param errorTag tag for text from resource with error prefix
     * @return row of keyboard which contain error message as inline button
     */
    private List<InlineKeyboardButton> getErrorButton(String errorTag) {
        if (errorTag == null) return List.of();

        InlineKeyboardButton errorButton = new InlineKeyboardButton();
        errorButton.setText("⚠️ " + messageService.getMessage(errorTag, languageTag));
        errorButton.setCallbackData("error");

        return List.of(errorButton);
    }

    private SendMessage getSendMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.setParseMode(markdown);
        return sendMessage;
    }

    private EditMessageText getEditMessage(long chatId, String text, InlineKeyboardMarkup keyboard, Message message) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(keyboard);
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setParseMode(markdown);
        return editMessageText;
    }
}