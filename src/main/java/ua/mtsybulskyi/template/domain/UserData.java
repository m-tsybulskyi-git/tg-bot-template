package ua.mtsybulskyi.template.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ValueGenerationType;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserData implements Serializable {
    @Id @GeneratedValue
    int id;

    long chatId;
    int age;

    String name;
    String surname;
    String gender;
    String email;
    String language;
    String roles;
    String phone;
    String state;

    @Column(columnDefinition = "blob")
    Message message;

}
