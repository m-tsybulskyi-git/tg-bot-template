package ua.mtsybulskyi.template.botapi;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BotState {
    START,

    MENU_MAIN,
    MENU_SETTINGS,

    PROFILE_EDIT,

    PROFILE_AGE,
    PROFILE_EMAIL,
    PROFILE_FILLED,
    PROFILE_GET_AGE,
    PROFILE_GENDER,
    PROFILE_NAME,
    PROFILE_SURNAME,

    SETTINGS_LANGUAGE,
    SETTINGS_PROFILE,
    SETTINGS_CHANGE_ROLES,

    ROLES_EDIT,
}
