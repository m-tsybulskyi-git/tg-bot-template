package ua.mtsybulskyi.template.repository;

import ua.mtsybulskyi.template.domain.UserData;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsersProfileRepository extends CrudRepository<UserData, Long> {
    UserData findByChatId(long chatId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tg_db.user_data SET language = ?2 WHERE chat_id = ?1",
            nativeQuery = true)
    void setLanguageTag(long chatId, String languageTag);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tg_db.user_data SET gender = ?2 WHERE chat_id = ?1",
            nativeQuery = true)
    void updateGender(long chatId, String languageTag);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tg_db.user_data SET age = ?2 WHERE chat_id = ?1",
            nativeQuery = true)
    void updateAge(long chatId, int age);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tg_db.user_data SET state = ?2 WHERE chat_id = ?1",
            nativeQuery = true)
    void setBotState(long chatId, String state);

    @Transactional
    @Query(value = "SELECT language FROM tg_db.user_data WHERE chat_id = ?1",
            nativeQuery = true)
    String getLanguageTag(long chatId);

    @Transactional
    @Query(value = "SELECT state FROM tg_db.user_data WHERE chat_id = ?1",
            nativeQuery = true)
    String getUserBotState(long chatId);

    @Transactional
    @Query(value = "SELECT age FROM tg_db.user_data WHERE chat_id = ?1",
            nativeQuery = true)
    int getAge(long chatId);

}
