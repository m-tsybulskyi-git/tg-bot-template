package ua.mtsybulskyi.template.botapi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.*;
import ua.mtsybulskyi.template.QuestionAnsweringBot;
import ua.mtsybulskyi.template.data.Security;
import ua.mtsybulskyi.template.domain.Roles;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.service.HandlerService;
import ua.mtsybulskyi.template.service.UserDataService;

import java.io.InputStream;
import java.net.URL;

@Component
@Slf4j
public class TelegramFacade {
    private final HandlerService handlerService;
    private final UserDataService userDataService;
    private final QuestionAnsweringBot questionAnsweringBot;
    private final Security security;

    public TelegramFacade(@Lazy QuestionAnsweringBot questionAnsweringBot,
                          @Lazy HandlerService handlerService, UserDataService userDataService, Security security) {
        this.handlerService = handlerService;
        this.userDataService = userDataService;
        this.questionAnsweringBot = questionAnsweringBot;
        this.security = security;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        BotApiMethod<?> replyMessage = null;


        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            questionAnsweringBot.sendAnswerCallbackQuery("", false, callbackQuery);
            return handleInput(callbackQuery);
        }

        Message message = update.getMessage();
        if (message != null) {
            if (message.hasDocument()) {
                downloadFile(message.getDocument());
                if (security.compareAdminKeys()) {
                    userDataService.setRole(message.getChatId(),
                            Roles.ADMIN_ROLE.toString());

                    userDataService.setBotState(message.getChatId(),
                            BotState.SETTINGS_CHANGE_ROLES);
                    replyMessage = handleInput(message);

                }

            } else if (message.hasText()) {
                questionAnsweringBot.deleteMessage(message);

                replyMessage = handleInput(message);
            }

            questionAnsweringBot.deleteMessage(message);
        }
        return replyMessage;
    }

    private BotApiMethod<?> handleInput(CallbackQuery callbackQuery) {
        BotState botState = userDataService.getUserState(callbackQuery.getMessage().getChatId());
        return handlerService.processInputMessage(botState, callbackQuery);
    }

    private BotApiMethod<?> handleInput(Message message) {
        long chatId = message.getChatId();
        UserData user = userDataService.getUserData(chatId);

        BotState botState;
        BotApiMethod<?> reply;

        boolean startMessageException = false;
        if (message.hasText()) {
            botState = switch (message.getText()) {
                case "/start" -> {

                    userDataService.saveStartUserData(message);
                    if (user.getMessage() != null)
                        questionAnsweringBot.deleteMessage(user.getMessage());
                    yield BotState.START;
                }

                default -> {
                    if (userDataService.getUserState(chatId).equals(BotState.START)) startMessageException = true;
                    yield userDataService.getUserState(chatId);
                }
            };

            if (startMessageException) return null;

            user.setBotState(botState);
            reply = handlerService.processInputMessage(botState, message);

        } else {
            botState = userDataService.getUserState(chatId);
            reply = handlerService.processInputMessage(botState, user.getMessage());
        }

        return reply;
    }

    @SneakyThrows
    public File downloadFile(Document document) {
        File file = getFilePath(document);

        java.io.File localFile = new java.io.File("src/main/resources/static/docs/userKey.txt");
        InputStream is = new URL(file.getFileUrl(questionAnsweringBot.getBotToken())).openStream();
        FileUtils.copyInputStreamToFile(is, localFile);

        return null;
    }

    @SneakyThrows
    public File getFilePath(Document document) {
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        File file = questionAnsweringBot.execute(getFile);
        return file;
    }
}