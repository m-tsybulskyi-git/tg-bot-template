package ua.mtsybulskyi.template.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.botapi.handlers.InputHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HandlerService {
    private final Map<BotState, InputHandler> messageHandlers = new HashMap<>();

    public HandlerService(List<InputHandler> messageHandlers) {
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getHandlerName(), handler));
    }

    public InputHandler getHandler(BotState botState){
        return messageHandlers.get(botState);
    }

    public InputHandler findMessageHandler(BotState currentState) {
        return switch(currentState){
            case PROFILE_AGE,PROFILE_EDIT, PROFILE_EMAIL, PROFILE_GET_AGE, PROFILE_FILLED,
                    PROFILE_GENDER, PROFILE_NAME, PROFILE_SURNAME -> getHandler(BotState.PROFILE_EDIT);
            default -> getHandler(currentState);
        };
    }

    public BotApiMethod<?> processInputMessage(BotState currentState, Message message) {
        InputHandler currentMessageHandler = findMessageHandler(currentState);
        return currentMessageHandler.handle(message);
    }

    public BotApiMethod<?> processInputMessage(BotState currentState, CallbackQuery callbackQuery) {
        InputHandler currentMessageHandler = findMessageHandler(currentState);
        return currentMessageHandler.handle(callbackQuery);
    }
}
