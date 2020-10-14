package ua.mtsybulskyi.template.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionAnswerData {
    @Id @GeneratedValue
    int id;
    String question;
    String answer;
    int user_id_question;
    int user_id_answer;
    String status;
}
