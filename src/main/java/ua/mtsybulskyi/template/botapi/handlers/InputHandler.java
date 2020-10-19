package ua.mtsybulskyi.template.botapi.handlers;

import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InputHandler {
    protected static final Logger log = org.slf4j.LoggerFactory.getLogger(InputHandler.class);
    public final LocaleMessageService messageService;
    public final UserDataService userDataService;
    public final HandlerService handlerService;

    private final String markdown = ParseMode.HTML;

    public String localeTag;

    public abstract BotApiMethod<?> handle(Message message);

    public abstract BotApiMethod<?> handle(CallbackQuery callbackQuery);

    public abstract BotState getHandlerName();

    public abstract BotState getPreviousHandlerName();

    protected InputHandler(LocaleMessageService messageService,
                           UserDataService userDataService,
                           HandlerService handlerService) {
        this.messageService = messageService;
        this.userDataService = userDataService;
        this.handlerService = handlerService;
    }

    protected BotApiMethod<?> getReplyMessage(Message inputMessage, String messageText,
                                              boolean newMessage, String error, Object... args) {
        localeTag = userDataService.getLanguageTag(inputMessage.getChatId());
        UserData user = userDataService.getUserData(inputMessage.getChatId());
        if(messageText == null || messageText.isEmpty()) {
            user.setBotState(getPreviousHandlerName());
            return redirectFromMessage(inputMessage, getPreviousHandlerName());
        }

        String text =  messageService.getMessage(messageText, localeTag, args);
        return  getReplyMessage(inputMessage, text, newMessage, true, error);
    }

    protected BotApiMethod<?> getReplyMessage(Message inputMessage,
                                              String messageText,
                                              boolean newMessage,
                                              List<List<InlineKeyboardButton>> keyboard,
                                              String error,
                                              Object... args) {
        if (newMessage) {
            SendMessage sendMessage = (SendMessage) getReplyMessage(inputMessage, messageText, true, error, args);
            sendMessage.setReplyMarkup(getInlineKeyboard(error, keyboard));
            return sendMessage;
        } else {
            EditMessageText editMessageText = (EditMessageText) getReplyMessage(inputMessage, messageText, false, error, args);
            editMessageText.setReplyMarkup(getInlineKeyboard(error, keyboard));
            return editMessageText;
        }
    }

    protected BotApiMethod<?> getReplyMessage(Message inputMessage,
                                              String messageText,
                                              boolean newMessage,
                                              boolean currentMessage,
                                              String error) {
        localeTag = userDataService.getLanguageTag(inputMessage.getChatId());
        long chatId = inputMessage.getChatId();

        if (newMessage) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(inputMessage.getChatId());
            sendMessage.setText(messageText);
            sendMessage.setReplyMarkup(getInlineKeyboard(error, chatId));
            sendMessage.setParseMode(markdown);
            return sendMessage;
        } else {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(inputMessage.getChatId());
            editMessageText.setText(messageText);
            editMessageText.setReplyMarkup(getInlineKeyboard(error, chatId));
            editMessageText.setMessageId(inputMessage.getMessageId());
            editMessageText.setParseMode(markdown);
            return editMessageText;
        }
    }

    protected BotApiMethod<?> redirectFromCallback(CallbackQuery callbackQuery, Map<String, BotState> map) {
        Map<String, BotState> stateMap = new HashMap<>(Map.copyOf(map));
        long chatId = callbackQuery.getMessage().getChatId();
        UserData userData = userDataService.getUserData(chatId);


        stateMap.put("back", getPreviousHandlerName());
        BotState botState = stateMap.get(callbackQuery.getData());

        if (botState == null) {
            AnswerCallbackQuery errorCallbackQuery = new AnswerCallbackQuery();
            errorCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            log.info("error");
            return errorCallbackQuery;
        }

        userData.setMessage(callbackQuery.getMessage());
        userData.setBotState(botState);
        log.info(botState.toString());

        InputHandler inputHandler = handlerService.findMessageHandler(botState);

        Message message = callbackQuery.getMessage();
        return inputHandler.handle(message);
    }

    protected BotApiMethod<?> redirectFromMessage(Message message, BotState botState) {
        long chatId = message.getChatId();
        userDataService.setBotState(chatId, botState);
        return handlerService.getHandler(botState).handle(message);
    }

    /******************** Keyboard ******************/

    protected InlineKeyboardMarkup getInlineKeyboard(String error, long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> errorRow = getErrorButton(error);

        List<List<InlineKeyboardButton>> keyboard = new java.util.ArrayList<>(List.copyOf(getKeyboard(chatId)));

        if (!errorRow.isEmpty()) keyboard.add(errorRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup getInlineKeyboard(String error, List<List<InlineKeyboardButton>> keyboardCustom) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> errorRow = getErrorButton(error);

        List<List<InlineKeyboardButton>> keyboard = new java.util.ArrayList<>(List.copyOf(keyboardCustom));

        if (!errorRow.isEmpty()) keyboard.add(errorRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    protected List<InlineKeyboardButton> getBackButton() {
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(messageService.getMessage("menu.back", localeTag));
        button.setCallbackData("back");

        return List.of(button);
    }

    protected List<InlineKeyboardButton> getErrorButton(String error) {
        if (error == null) return List.of();

        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText("⚠️ " + messageService.getMessage(error, localeTag));
        button.setCallbackData("error");

        return List.of(button);
    }

    abstract protected List<List<InlineKeyboardButton>> getKeyboard(long chatId);
}
