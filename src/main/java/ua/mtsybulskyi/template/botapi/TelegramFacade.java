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
import ua.mtsybulskyi.template.data.FileKey;
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
    private final FileKey fileKey;

    public TelegramFacade(@Lazy QuestionAnsweringBot questionAnsweringBot,
                          @Lazy HandlerService handlerService, UserDataService userDataService, FileKey fileKey) {
        this.handlerService = handlerService;
        this.userDataService = userDataService;
        this.questionAnsweringBot = questionAnsweringBot;
        this.fileKey = fileKey;
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
            replyMessage = handleInput(message);
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
        BotApiMethod<?> reply = null;

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
                    if (userDataService.getUserState(chatId).equals(BotState.START))
                        startMessageException = true;
                    yield userDataService.getUserState(chatId);
                }
            };

            if (startMessageException) return null;

            user.setBotState(botState);
            reply = handlerService.processInputMessage(botState, message);

        } else if (message.hasDocument()) {
            downloadFile(message.getDocument());
            if (fileKey.compareAdminKeys()) { // < if file text has key from telegram bot
                userDataService.setRole(message.getChatId(),
                        Roles.ADMIN_ROLE.toString());

                userDataService.setBotState(message.getChatId(),
                        BotState.SETTINGS_CHANGE_ROLES);
            }

            botState = userDataService.getUserState(chatId);
            reply = handlerService.processInputMessage(botState, user.getMessage());
        }

        return reply;
    }

    @SneakyThrows
    public void downloadFile(Document document) {
        File file = getFilePath(document);
        if (file == null) return;

        java.io.File localFile = new java.io.File("src/main/resources/static/docs/userKey.txt");
        InputStream is = new URL(file.getFileUrl(questionAnsweringBot.getBotToken())).openStream();
        FileUtils.copyInputStreamToFile(is, localFile);
    }

    @SneakyThrows
    public File getFilePath(Document document) {
        if (document.getFileName().equals("botKey.txt") && document.getFileSize() == fileKey.getLength()) {
            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            return questionAnsweringBot.execute(getFile);
        }

        return null;
    }
}