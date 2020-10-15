package ua.mtsybulskyi.template.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.mtsybulskyi.template.botapi.BotState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserData implements Serializable {
    @Id
    @GeneratedValue()
    int id;

    long chatId;
    int age;

    String firstName;
    String lastName;
    String gender;
    String email;
    String language;

    @Enumerated(EnumType.STRING)
    BotState botState;

    @Column(columnDefinition = "blob")
    Message message;

    String password;

    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;
}
