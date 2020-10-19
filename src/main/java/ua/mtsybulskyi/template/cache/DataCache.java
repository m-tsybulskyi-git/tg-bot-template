package ua.mtsybulskyi.template.cache;

import org.springframework.stereotype.Component;
import ua.mtsybulskyi.template.domain.UserData;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataCache {
    private final Map<Long, UserData> editRoles = new HashMap<>();

    public void putUserData(long chatId, UserData userData) {
        editRoles.put(chatId, userData);
    }

    public UserData getUserData(long chatId) {
        return editRoles.get(chatId);
    }
}
