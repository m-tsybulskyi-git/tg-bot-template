package ua.mtsybulskyi.template.service;

import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.repository.UsersProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
public class UserDataService {

    private final UsersProfileRepository profileRepository;

    public UserDataService(UsersProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public boolean saveStartUserData(Message message){
        User user = message.getFrom();
        UserData userData = new UserData();

        if(profileRepository.findByChatId(message.getChatId()) == null) {
            userData.setChatId(message.getChatId());
            userData.setName(user.getFirstName());
            userData.setSurname(user.getLastName());
            userData.setGender("\uD83D\uDEAB");
            userData.setEmail("\uD83D\uDEAB");
            userData.setAge(0);
            userData.setLanguage("eu-EU");
            userData.setState(BotState.START.toString());
            saveUserProfileData(userData);
            return true;
        }
        return false;
    }

    public void saveUserProfileData(UserData userData) {
        profileRepository.save(userData);
    }

    public UserData getUserProfileData(long chatId) {
        return profileRepository.findByChatId(chatId);
    }

    public void setName(long chatId, String name){
        getUserProfileData(chatId).setName(name);
    }

    public void setSurname(long chatId, String surname){
        getUserProfileData(chatId).setSurname(surname);
    }

    public void setEmail(long chatId, String email){
        getUserProfileData(chatId).setEmail(email);
    }

    public void setGender(long chatId, String gender){
        if(gender.equals("man") || gender.equals("woman")) profileRepository.updateGender(chatId, gender);
        else throw new NullPointerException();
    }

    public void setAge(long chatId, int age){
        profileRepository.updateAge(chatId, age);
    }

    public void setLanguageTag(long chatId, String languageTag){ profileRepository.setLanguageTag(chatId, languageTag); }

    public String getLanguageTag(long chatId){
        return profileRepository.getLanguageTag(chatId);
    }

    public String getLanguageTag(Message message){
         return profileRepository.getLanguageTag(message.getChatId());
    }

    public void setUserState(long chatId, BotState botState) {
        profileRepository.setBotState(chatId, botState.toString());
    }

    public BotState getUserState(long chatId) {
        String botState = profileRepository.getUserBotState(chatId);
        if(botState == null) return BotState.START;
        return BotState.valueOf(botState);
    }

    public void setLastMessageFromBot(long chatId, Message message) {
        profileRepository.findByChatId(chatId).setMessage(message);
    }

    public Message getLastMessageFromBot(long chatId) {
        return profileRepository.findByChatId(chatId).getMessage();
    }

    public String getName(long chatId) {
        return profileRepository.findByChatId(chatId).getName();
    }

    public String getSurname(long chatId) {
        return profileRepository.findByChatId(chatId).getSurname();
    }

    public String getAge(long chatId) {
        int age = profileRepository.getAge(chatId);
        if(age <= 0) return "\uD83D\uDEAB";
       return String.valueOf(age);
    }
}
