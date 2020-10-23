package ua.mtsybulskyi.template.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ua.mtsybulskyi.template.botapi.BotState;
import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.domain.Roles;
import ua.mtsybulskyi.template.domain.UserData;
import ua.mtsybulskyi.template.repository.PrivilegeRepository;
import ua.mtsybulskyi.template.repository.RoleRepository;
import ua.mtsybulskyi.template.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Transactional
public class UserDataService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    String notFound = Emoji.DENY.toString();

    public UserDataService(UserRepository userRepository, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Transactional
    public void saveStartUserData(Message message) {
        User user = message.getFrom();
        UserData userData = new UserData();

        if (userRepository.findByChatId(message.getChatId()) == null) {
            userData.setChatId(message.getChatId());
            if (user.getFirstName() != null) {
                userData.setFirstName(user.getFirstName());
            } else {
                userData.setFirstName(" ");
            }

            if (user.getLastName() != null) {
                userData.setLastName(user.getLastName());
            } else {
                userData.setLastName(" ");
            }

            userData.setRoles(getRole("USER_ROLE"));

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
    public String getFirstName(long chatId) {
        String firstName = userRepository.findByChatId(chatId).getFirstName();
        if (validation(firstName)) return notFound;
        return firstName;
    }

    @Transactional
    public String getLastName(long chatId) {
        String lastName = userRepository.findByChatId(chatId).getLastName();
        if (validation(lastName)) return notFound;
        return lastName;
    }

    @Transactional
    public String getEmail(long chatId) {
        String email = userRepository.findByChatId(chatId).getEmail();
        if (validation(email)) return notFound;
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
        if (validation(languageTag)) return "eu-EU";

        return languageTag;
    }

    @Transactional
    public BotState getUserState(long chatId) {
        String botState;
        try {
            botState = userRepository.findByChatId(chatId).getBotState().toString();
        } catch (NullPointerException e) {
            return BotState.START;
        }

        return BotState.valueOf(botState);
    }

    @Transactional
    public String getUserRoleString(long chatId) {
        UserData user = userRepository.findByChatId(chatId);
        StringBuilder stringBuilder = new StringBuilder();
        Collection<Role> userRoles = user.getRoles();
        if (userRoles != null) {
            userRoles.forEach(x -> {
                if (x != null) stringBuilder.append(x.getName());
            });

            return stringBuilder.toString();
        }

        return null;
    }

    @Transactional
    public Role getUserRole(long chatId) {
        UserData user = userRepository.findByChatId(chatId);
        Collection<Role> userRoles = user.getRoles();
        AtomicReference<Role> role = new AtomicReference<>();
        if (userRoles != null) {
            userRoles.forEach(x -> {
                if (x != null) role.set(x);
            });
        }

        return role.get();
    }

    @Transactional
    public boolean hasPrivilege(long chatId, String name) {
        AtomicBoolean hasPrivileges = new AtomicBoolean(false);
        Role role = roleRepository.findByName(getUserRoleString(chatId));
        if (role != null) {
            role.getPrivileges().forEach(x -> {
                if (x.getName().equals(name))
                    hasPrivileges.set(true);
            });
        }

        return hasPrivileges.get();
    }

    @Transactional
    public List<UserData> getUsers(boolean sorted) {
        ArrayList<UserData> usersData = new ArrayList<>();
        if (sorted) {
            List<Roles> allRoles = Arrays.asList(Roles.class.getEnumConstants());
            ArrayList<UserData> finalUsersData = usersData;
            allRoles.forEach(x -> {
                Role role = roleRepository.findByName(x.toString());
                finalUsersData.addAll(userRepository.findAllByRoles(role));
            });
        } else {
            usersData = Lists.newArrayList(userRepository.findAll());
        }

        return usersData;
    }

    @Transactional
    public List<Role> getRoles(boolean sorted) {
        List<Roles> roles = Arrays.asList(Roles.class.getEnumConstants());
        List<Role> userRoles = new ArrayList<>();

        if (sorted) {
            List<Role> finalUserRoles = userRoles;
            roles.forEach(x -> finalUserRoles
                    .add(roleRepository.findByName(x.toString())));
        } else {
            userRoles = Lists.newArrayList(roleRepository.findAll());
        }

        return userRoles;
    }

    @Transactional
    public List<Role> getRole(String name) {
        return Arrays.asList(roleRepository.findByName(name));
    }

    @Transactional
    public boolean setRole(long chatId, String name) {
        List<Role> role = getRole(name);
        getUserData(chatId).setRoles(role);
        return getUserData(chatId).getRoles().equals(role);
    }

    @Transactional
    public boolean setBotState(long chatId, BotState botState) {
        getUserData(chatId).setBotState(botState);
        return getUserData(chatId).getBotState().equals(botState);
    }

    private boolean validation(String text) {
        return text == null || text.isEmpty();
    }

    private boolean validation(int integer) {
        return integer <= 0;
    }
}
