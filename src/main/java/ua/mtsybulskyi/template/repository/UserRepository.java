package ua.mtsybulskyi.template.repository;

import ua.mtsybulskyi.template.domain.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends CrudRepository<UserData, Long> {
    UserData findByChatId(long chatId);
}
