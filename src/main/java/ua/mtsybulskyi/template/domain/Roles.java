package ua.mtsybulskyi.template.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Roles implements Comparable<Roles>{
    ADMIN_ROLE(1),
    WORKER_ROLE(2),
    USER_ROLE(3);

    int priority;

    public int getPriority() {
        return priority;
    }
}
