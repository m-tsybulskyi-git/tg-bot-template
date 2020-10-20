package ua.mtsybulskyi.template.service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Emoji {
    CURRENT("✅"),
    ADMIN("⚜️"),
    WORKER("\uD83D\uDCB8"),
    DENY("\uD83D\uDEAB");

    private final String emoji;

    @Override
    public String toString() {
        return emoji;
    }
}
