package ua.mtsybulskyi.template.botapi;

import ua.mtsybulskyi.template.QuestionAnsweringBot;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class TelegramFacade {
    private final HandlerService handlerService;
    private final UserDataService userDataService;
    private final QuestionAnsweringBot questionAnsweringBot;

    public TelegramFacade(@Lazy QuestionAnsweringBot questionAnsweringBot,
                          @Lazy HandlerService handlerService, UserDataService userDataService) {
        this.handlerService = handlerService;
        this.userDataService = userDataService;
        this.questionAnsweringBot = questionAnsweringBot;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        BotApiMethod<?> replyMessage = null;

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            questionAnsweringBot.sendAnswerCallbackQuery("", false, callbackQuery);
            return handleInput(callbackQuery);
        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            questionAnsweringBot.deleteMessage(message);
            replyMessage = handleInput(message);
        }
        return replyMessage;
    }

    private BotApiMethod<?> handleInput(CallbackQuery callbackQuery) {
        BotState botState = userDataService.getUserState(callbackQuery.getMessage().getChatId());
        return handlerService.processInputMessage(botState, callbackQuery);
    }

    private BotApiMethod<?> handleInput(Message message) {
        long chatId = message.getChatId();
        BotState botState;
        BotApiMethod<?> reply;

        botState = switch (message.getText()) {
            case "/start" -> {
                userDataService.saveStartUserData(message);
                if(userDataService.getLastMessageFromBot(chatId) != null)
                    questionAnsweringBot.deleteMessage(userDataService.getLastMessageFromBot(chatId));
                yield BotState.START;
            }

            default -> userDataService.getUserState(chatId);
        };
        userDataService.setUserState(chatId, botState);
        reply = handlerService.processInputMessage(botState, message);
        return reply;
    }
}