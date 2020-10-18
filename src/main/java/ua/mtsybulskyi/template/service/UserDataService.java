package ua.mtsybulskyi.template.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.repository.RoleRepository;
import ua.mtsybulskyi.template.repository.UserRepository;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Service
@Transactional
public class UserDataService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    String notFound = "\uD83D\uDEAB";

    public UserDataService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void saveStartUserData(Message message) {
        User user = message.getFrom();
        UserData userData = new UserData();

        if (userRepository.findByChatId(message.getChatId()) == null) {
            userData.setChatId(message.getChatId());

            userData.setFirstName(user.getFirstName());
            userData.setLastName(user.getLastName());
            userData.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));

            saveUserData(userData);
        }
    }

    @Transactional
    public void saveUserData(UserData userData) {
        userRepository.save(userData);
    }

    @Transactional
    public UserData getUserData(long chatId) {
        return userRepository.findByChatId(chatId);
    }

    @Transactional
    public String getFirstName(long chatId){
        String firstName = userRepository.findByChatId(chatId).getFirstName();
        if(validation(firstName)) return notFound;
        return firstName;
    }

    @Transactional
    public String getLastName(long chatId){
        String lastName = userRepository.findByChatId(chatId).getLastName();
        if(validation(lastName)) return notFound;
        return lastName;
    }

    @Transactional
    public String getEmail(long chatId){
        String email = userRepository.findByChatId(chatId).getEmail();
        if(validation(email)) return notFound;
        return email;
    }

    @Transactional
    public String getGender(long chatId) {
        String gender = userRepository.findByChatId(chatId).getGender();
        if (validation(gender)) return notFound;

        return gender;
    }

    @Transactional
    public String getAge(long chatId) {
        int age = userRepository.findByChatId(chatId).getAge();
        if (validation(age)) return notFound;
        return String.valueOf(age);
    }


    @Transactional
    public String getLanguageTag(long chatId) {
        String languageTag = userRepository.findByChatId(chatId).getLanguage();
        if(validation(languageTag)) return "eu-EU";

        return languageTag;
    }

    @Transactional
    public BotState getUserState(long chatId) {
        String botState = userRepository.findByChatId(chatId).getBotState().toString();
        if (botState == null) return BotState.START;
        return BotState.valueOf(botState);
    }

    @Transactional
    public String getUserRole(long chatId) {
        Collection<Role> roles = userRepository.findByChatId(chatId).getRoles();
        return roles.toString();
    }
/*******************Validation************************/

    private boolean validation(String text){
        return text == null || text.isEmpty();
    }

    private boolean validation(int integer){
        return integer <= 0;
    }
}
